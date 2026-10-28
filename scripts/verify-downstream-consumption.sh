#!/usr/bin/env bash
#
# 下游消费冒烟验证 —— 以「真实用户视角」验证发布件可用
#
# 背景：`mvn install EXIT=0` 只证明本仓不报错，证明不了下游用户不报错
#       （来源：本项目 CLAUDE.md 开发红线总表「破坏性删除多源验证」条目）
# 本脚本：建一个干净的下游 Spring Boot 工程 → 依赖本地仓库的发布件 →
#         写一个零注解的 @RestController → 编译并断言 MCP 工具已生成
#
# 用法：
#   bash scripts/verify-downstream-consumption.sh            # 仅解析依赖 + 编译
#   bash scripts/verify-downstream-consumption.sh --run      # 额外启动并断言工具清单
#
set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORK_DIR="$(mktemp -d "${TMPDIR:-/tmp}/s2mcp-downstream-XXXXXX")"
VERSION="${VERSION:-$(mvn -q -N -f "${REPO_ROOT}/pom.xml" help:evaluate -Dexpression=project.version -DforceStdout 2>/dev/null | tail -1)}"
STARTER="${STARTER:-server2mcp-starter-webmvc}"
RUN_APP=0
[ "${1:-}" = "--run" ] && RUN_APP=1

PASS=0; FAIL=0
ok()  { printf '  \033[32m✓\033[0m %s\n' "$1"; PASS=$((PASS+1)); }
bad() { printf '  \033[31m✗\033[0m %s\n' "$1"; FAIL=$((FAIL+1)); }
sec() { printf '\n\033[1m%s\033[0m\n' "$1"; }

cleanup() { rm -rf "${WORK_DIR}"; }
trap cleanup EXIT

printf '==============================================\n'
printf ' 下游消费冒烟验证\n'
printf ' 坐标：io.github.theeterna:%s:%s\n' "${STARTER}" "${VERSION}"
printf ' 临时工程：%s\n' "${WORK_DIR}"
printf '==============================================\n'

# ---------------------------------------------------------------- 0. 前置：发布件已入本地仓库
sec "0. 本地仓库中的发布件"
JAR_PATH="${HOME}/.m2/repository/io/github/theeterna/${STARTER}/${VERSION}/${STARTER}-${VERSION}.jar"
if [ -f "${JAR_PATH}" ]; then
  ok "本地仓库已存在 ${STARTER}-${VERSION}.jar"
else
  bad "本地仓库缺 ${STARTER}-${VERSION}.jar —— 先执行 mvn clean install"
  exit 1
fi

# ---------------------------------------------------------------- 1. 构造干净下游工程
sec "1. 构造干净下游工程"
mkdir -p "${WORK_DIR}/src/main/java/com/example/demo" "${WORK_DIR}/src/main/resources"
cat > "${WORK_DIR}/pom.xml" <<POM
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>downstream-smoke</artifactId>
    <version>1.0.0</version>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.1.0</version>
        <relativePath/>
    </parent>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>io.github.theeterna</groupId>
            <artifactId>${STARTER}</artifactId>
            <version>${VERSION}</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
POM
ok "pom.xml 已生成"

# ---------------------------------------------------------------- 2. 零注解 Controller（验证非侵入式）
cat > "${WORK_DIR}/src/main/java/com/example/demo/OrderController.java" <<'JAVA'
package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 下游用户的普通 Controller —— 不加任何 MCP 注解。
 * 在 interface 作用域下，其方法应自动成为 MCP 工具。
 */
@RestController
public class OrderController {

    /** 查询订单 */
    @GetMapping("/orders/{id}")
    public String getOrder(Long id) {
        return "order-" + id;
    }
}
JAVA
ok "OrderController 已生成（零 MCP 注解）"

# 调试端点：下游用户排查「我的接口为什么没变成工具」的常规手法 —— 读公开的 IToolContext 注册表
cat > "${WORK_DIR}/src/main/java/com/example/demo/DebugController.java" <<'JAVA'
package com.example.demo;

import com.ai.plug.core.context.tool.IToolContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 下游用户的排查视角：直接读框架的公开注册表，看自己的 Controller 有没有进来。 */
@RestController
public class DebugController {

    private final IToolContext toolContext;

    public DebugController(IToolContext toolContext) {
        this.toolContext = toolContext;
    }

    @GetMapping("/debug/tools")
    public List<String> tools() {
        return toolContext.getRawTools().keySet().stream().sorted().toList();
    }
}
JAVA
ok "DebugController 已生成（注入公开的 IToolContext）"

cat > "${WORK_DIR}/src/main/java/com/example/demo/DemoApplication.java" <<'JAVA'
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
JAVA
ok "DemoApplication 已生成"

cat > "${WORK_DIR}/src/main/resources/application.yml" <<'YAML'
server:
  port: 18099
plugin:
  mcp:
    enabled: true
    scope: interface
YAML
ok "application.yml 已生成（scope=interface）"

# ---------------------------------------------------------------- 3. 依赖解析
sec "3. 依赖解析（下游视角）"
if mvn -q -f "${WORK_DIR}/pom.xml" dependency:resolve > "${WORK_DIR}/resolve.log" 2>&1; then
  ok "发布件及其传递依赖全部可解析"
else
  bad "依赖解析失败："
  tail -20 "${WORK_DIR}/resolve.log" | sed 's/^/      /'
  exit 1
fi

# ---------------------------------------------------------------- 4. 编译
sec "4. 编译下游工程"
if mvn -q -f "${WORK_DIR}/pom.xml" -DskipTests compile > "${WORK_DIR}/compile.log" 2>&1; then
  ok "下游工程编译通过"
else
  bad "编译失败："
  tail -20 "${WORK_DIR}/compile.log" | sed 's/^/      /'
  exit 1
fi

# ---------------------------------------------------------------- 5. 运行期断言（非侵入式生效）
if [ "${RUN_APP}" = "1" ]; then
  sec "5. 启动下游应用并断言工具已生成"
  mvn -q -f "${WORK_DIR}/pom.xml" spring-boot:run > "${WORK_DIR}/run.log" 2>&1 &
  APP_PID=$!
  READY=0
  for _ in $(seq 1 60); do
    sleep 1
    if curl -sf "http://localhost:18099/mcp/discover" > "${WORK_DIR}/discover.json" 2>/dev/null; then
      READY=1; break
    fi
    kill -0 "${APP_PID}" 2>/dev/null || break
  done

  if [ "${READY}" = "1" ]; then
    ok "下游应用启动成功，/mcp/discover 可达"

    # 断言 1 · 框架注册表（下游用户视角）
    # 注：注册表的 key 是【bean 名】（首字母小写的类名），暴露给 MCP 客户端的工具名才是 className_methodName
    TOOLS=$(curl -sf --max-time 5 "http://localhost:18099/debug/tools" 2>/dev/null || true)
    printf '%s' "${TOOLS}" > "${WORK_DIR}/tools.json"
    if printf '%s' "${TOOLS}" | grep -qi "orderController"; then
      ok "非侵入式生效：零 MCP 注解的 OrderController 已进入 MCP 工具注册表"
      printf '%s' "${TOOLS}" | head -c 300; echo
    else
      bad "注册表中未见 orderController —— interface 作用域扫描未生效"
      printf '%s' "${TOOLS}" | head -c 400; echo
    fi

    # 断言 2 · SDK 侧真实注册（服务端启动日志）
    if grep -qE "Registered tools: [1-9]" "${WORK_DIR}/run.log" 2>/dev/null; then
      ok "SDK 侧已注册工具：$(grep -oE 'Registered tools: [0-9]+' "${WORK_DIR}/run.log" | head -1)"
    else
      bad "SDK 启动日志未见 Registered tools 计数 —— 工具未真正挂到 MCP Server"
      grep -iE "Registered tools|No tool methods" "${WORK_DIR}/run.log" 2>/dev/null | head -3 | sed 's/^/      /'
    fi

    # 断言 3 · 启动日志不得出现框架引发的噪声告警
    NOISE=$(grep -cE "Cannot enhance @Configuration bean definition|parsing module you specified|JavaDoc解析失败" "${WORK_DIR}/run.log" 2>/dev/null || true)
    if [ "${NOISE}" = "0" ]; then
      ok "启动日志无框架引发的噪声告警（配置类增强 / 解析兜底 / Javadoc 跳过）"
    else
      bad "启动日志出现 ${NOISE} 条框架噪声告警："
      grep -E "Cannot enhance @Configuration bean definition|parsing module you specified|JavaDoc解析失败" "${WORK_DIR}/run.log" 2>/dev/null | head -3 | sed 's/^/      /'
    fi
  else
    bad "下游应用未能在 60s 内就绪"
    tail -30 "${WORK_DIR}/run.log" | sed 's/^/      /'
  fi

  kill "${APP_PID}" 2>/dev/null
  wait "${APP_PID}" 2>/dev/null

  # ---------------------------------------------------------------- 6. 解析器名校验（注册纪律 R2）
  sec "6. 解析器名拼错时 strict 模式必须拒绝启动"
  cat > "${WORK_DIR}/src/main/resources/application.yml" <<'YAML'
server:
  port: 18098
plugin:
  mcp:
    enabled: true
    scope: interface
    parser:
      des: MCPTOOL, JAVADOCS
      strict: true
YAML
  mvn -q -f "${WORK_DIR}/pom.xml" spring-boot:run > "${WORK_DIR}/run-strict.log" 2>&1 &
  STRICT_PID=$!
  STRICT_EXITED=0
  for _ in $(seq 1 60); do
    sleep 1
    if ! kill -0 "${STRICT_PID}" 2>/dev/null; then
      STRICT_EXITED=1
      break
    fi
  done

  if [ "${STRICT_EXITED}" = "1" ]; then
    if grep -q "JAVADOCS" "${WORK_DIR}/run-strict.log"; then
      ok "strict 模式：拼错的解析器名被拒绝启动，且提示中含未知名 JAVADOCS"
      grep -oE "plugin\.mcp\.parser 配置中存在未知的解析器名：[^\"]*" "${WORK_DIR}/run-strict.log" | head -1 | sed 's/^/      /'
    else
      bad "strict 模式：应用已退出，但日志中未见未知名提示"
      tail -20 "${WORK_DIR}/run-strict.log" | sed 's/^/      /'
    fi
  else
    bad "strict 模式：应用未按预期退出（应因未知名解析器而拒绝启动）"
    kill "${STRICT_PID}" 2>/dev/null
  fi
  kill "${STRICT_PID}" 2>/dev/null
  wait "${STRICT_PID}" 2>/dev/null
fi

# ---------------------------------------------------------------- 汇总
printf '\n==============================================\n'
printf ' 通过 %d · 失败 %d\n' "${PASS}" "${FAIL}"
printf '==============================================\n'
[ "${FAIL}" -gt 0 ] && { echo "❌ 下游消费验证失败。"; exit 1; }
echo "✅ 下游消费验证通过 —— 发布件对真实用户可用。"
exit 0
