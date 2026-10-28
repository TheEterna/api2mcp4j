> 维护者：api2mcp4j Team · 创建：2026-06-24（继承 ../real-agent 文档体系心法）
> 📖 参考文档 — 新人 / 新 AI 入门，按需查阅。

# 新人 / 新 AI 入门指南

欢迎加入 **api2mcp4j**（内部代号 server2mcp）。本项目是一个 Spring Boot Starter 框架，把现有 Spring `@RestController` 接口**零改动**暴露为 MCP（Model Context Protocol）的 Tool / Resource / Prompt / Complete。

无论你是人类工程师还是 AI Agent，按本文三步即可上手。

---

## 一、3 步上手

### Step 1 · 构建（mvn clean install）

⚠️ **关键前提**：本项目**自身的 5 个模块之间**靠本地仓库互相引用，**必须本地安装**才能解析。
（Spring AI / MCP Java SDK 等外部依赖已于 2026-07-30 全部落到 Maven Central 正式版，不再是 SNAPSHOT。）

```bash
# 在仓库根目录，按依赖流向（common→core→autoconfigure→starters）一次性安装全部模块
cd /Users/ls/code/api2mcp4j
mvn clean install
```

构建要求：

| 要求 | 说明 |
|---|---|
| Java 17 | 框架要求，低版本无法编译 |
| `-parameters` 编译参数 | 根 pom 已全局启用，用于反射获取方法参数名（解析器依赖） |

### Step 2 · 跑测试

测试集中在 `server2mcp-core` 模块：

```bash
# 运行 core 全部测试
cd /Users/ls/code/api2mcp4j/server2mcp-core && mvn test

# 运行单个测试类（例：JSON Schema 生成）
cd /Users/ls/code/api2mcp4j/server2mcp-core && mvn test -Dtest=GenSchemaUtilsTest
```

现有测试位于 `server2mcp-core/src/test`：**92 个测试类 / 583 个 `@Test`**，
分布在 `parser` / `provider` / `spec` / `utils` 四个能力域。新人按能力域定位：

| 能力域 | 验证内容 | 代表性测试 |
|---|---|---|
| `spec/` | 协议 2026-07-28 wire 层（20 个子域：jsonrpc / mrtr / tasks / capabilities / pagination / dedup / meta …） | `spec/jsonrpc/JsonRpcRouterTest` · `spec/mrtr/MrtrConversationEndToEndTest` · `spec/tasks/TasksEndpointTest` |
| `provider/` | 注册面 → 暴露面的桥接 | `provider/McpToolProviderConsistencyTest` · `provider/McpToolProviderIconMetaTest` |
| `parser/` | 描述/参数解析器责任链 | `parser/tool/des/JavaDocDesParserTest` · `parser/tool/starter/SingleStarterFallbackLoggingTest` |
| `utils/` | Schema 生成等工具 | `utils/GenSchemaUtilsTest` |

`TestClass` 为通用测试夹具，非测试类。

### Step 3 · 读懂一条处理链路

理解本框架的最快路径，是亲手追一条「Controller 方法 → MCP 工具」的链路。建议顺序：

```
1. com.ai.plug.core.annotation.ToolScan          先看入口注解，理解 @Import 触发机制
2. .../register/tool/McpToolScanRegistrar         ImportBeanDefinitionRegistrar 如何注册 Configurer
3. .../register/tool/McpToolScanConfigurer        在 BeanDefinitionRegistryPostProcessor 阶段驱动扫描
4. .../register/tool/ClassPathToolScanner         扫描命中类 → IToolContext.addTool
5. .../parser/tool/des/AbstractDesParser          双层解析器链如何按 @Order 解析描述/参数
6. .../spec/callback/tool/AbstractMcpToolMethodCallback   buildArgs 模板方法：参数注入 + 反射调用
7. .../provider/McpToolProvider                   桥接到 MCP SDK Specification（两级过滤）
```

> 完整链路图与数据流见 `docs/reference/architecture.md` §四「一个 Controller 方法如何变成 MCP 工具」。

---

## 二、必读文档清单

| 优先级 | 文档 | 用途 |
|---|---|---|
| ★★★ | 项目根 `CLAUDE.md` | 架构规范、模块依赖、6 大设计模式、配置参考、红线约定（权威事实源） |
| ★★★ | `docs/reference/architecture.md` | 系统架构总览、处理链路、双模式执行 |
| ★★☆ | `docs/reference/extension-points.md` | 扩展点手册（自定义解析器/转换器/过滤器/Context） |
| ★★☆ | `docs/rules/global/` | 全局工作纪律（删除纪律、重构顺序、会话连续性、工作留痕等） |
| ★★☆ | `docs/specs/` | 工程规范（文件头规范、注册纪律规范、工作日志规范） |
| ★☆☆ | `README.md` / `README_zh.md` | 项目对外说明、快速开始、Javadoc 解析器配置 |
| ★☆☆ | `docs/mcp-2026-07-28-INTEGRATION-MATRIX.md` | 协议 2026-07-28 逐字段合规快照（✅/🟡/❌ 三档） |

> `docs/logs/`、`docs/todos/`、`docs/plans/` 为团队工作留痕目录（计划/待办/日志），开工前先扫一眼有无相关在途项。

---

## 三、易踩坑清单（务必先读）

### 坑 1 · 依赖已全落正式版，但多模块仍需本地 install

> 2026-07-30 更新：Spring AI / MCP Java SDK 的 SNAPSHOT 依赖**已全部迁移到 Maven Central 正式版**
> （Spring Boot 4.1.0 · Spring AI 2.0.0 · MCP Java SDK 2.0.0 · Jackson 3 · victools 5.0.0），
> 不再有「拉不到依赖」的问题，API 也不会随时破坏性变更。

但本项目**自身的 5 个模块之间**仍靠本地仓库互相引用（`common ← core ← autoconfigure ← starters`），
所以 clone 后第一次使用仍需在根目录执行一次 `mvn clean install`，否则 `server2mcp-core` 找不到 `server2mcp-common` 等。

### 坑 2 · Javadoc 解析器需要拷贝 .java 源到 classpath

Javadoc 解析器本质是**解析源码文件**，而上线后 Java 以字节码 class 存在、不含 Javadoc。要用 `JavaDocDesParser` / `JavaDocParamParser`，必须用 `maven-resources-plugin` 把 `.java` 源拷进资源目录：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-resources-plugin</artifactId>
    <executions>
        <execution>
            <id>copy-java-sources</id>
            <phase>prepare-package</phase>
            <goals><goal>copy-resources</goal></goals>
            <configuration>
                <outputDirectory>${project.build.outputDirectory}</outputDirectory>
                <resources>
                    <resource>
                        <directory>src/main/java</directory>
                        <includes><include>**/*.java</include></includes>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

> 来源：`README_zh.md` §「注意：JAVADOC 解析器」

### 坑 3 · server2mcp-test 不在根 pom modules 中

演示应用 `server2mcp-test` **不在**根 pom 的 `<modules>` 里（根 pom 只含 common/core/autoconfigure/两个 starter）。`mvn clean install` 不会构建它，需**单独构建**：

```bash
cd /Users/ls/code/api2mcp4j/server2mcp-test && mvn clean package
```

### 坑 4 · 配置键是单数 `parser.param`（不是 `params`）

参数解析器开关的配置键为 `plugin.mcp.parser.param`（单数），与代码 `PluginProperties.Parser.param` 字段一致。描述解析器为 `plugin.mcp.parser.des`。两者均可省略，省略时按默认链注册。

### 坑 5 · `@Deprecated` / `@ToolNotScanForAuto` 会被自动排除

在 `interface` 作用域下，带 `@Deprecated` 或 `@ToolNotScanForAuto` 的类/方法**不会**被自动注册为工具。调试「为什么我的接口没暴露」时先查这两个注解。

### 坑 6 · OutputSchema 已实装（历史坑，已修复）

> 2026-08-03 更新：早期版本中 `outputSchema` 虽被解析，但在 `McpToolProvider` 构建 Specification 时被注释、改传 `null`
> （推测当时 MCP SDK 未支持）。**现已真实发送** —— `McpToolProvider` 中 `.outputSchema(outputSchema)` 生效，
> 出口为 `ToolDefinitionBuilder.buildToolOutputSchema()`。若你发现客户端收不到 `outputSchema`，先查工具方法的返回类型是否为空。

### 坑 7 · 协议新特性不在 SDK 里，而在 `core/spec/*`

MCP Java SDK 2.0 只实现到协议 `2025-11-25`，而本框架对外声明支持协议 `2026-07-28`。
差异部分由 `server2mcp-core` 的 `spec/` 层**自建 wire 表达**实现（JSON-RPC 路由、`server/discover`、`tasks/*`、
`subscriptions/listen`、MRTR、`_meta` 注入、capabilities schema 等）。

**因此：查协议 2026-07-28 新字段，先去 `core/spec/` 找，不要只在 SDK 里找。**
逐字段合规快照见 `docs/mcp-2026-07-28-INTEGRATION-MATRIX.md`，架构说明见 `docs/reference/architecture.md` §六。

---

## 四、最小可用配置（抄了就能跑）

```yaml
plugin:
  mcp:
    enabled: true          # 总开关
    scope: interface       # interface=自动扫描 Controller；custom=仅显式 @ToolScan
    parser:
      param: JAVADOC, TOOL, SpringMVC, JACKSON, SWAGGER2, SWAGGER3   # 可省略，用默认
      des:   JAVADOC, TOOL, JACKSON, SWAGGER3, SWAGGER2             # 可省略，用默认
    tool:     { enabled: true }
    resource: { enabled: true }
    prompt:   { enabled: true }
    complete: { enabled: true }
    root:     { enabled: true }
```

引入依赖（WebMVC 应用）：

```xml
<dependency>
    <groupId>com.ai.plug</groupId>
    <artifactId>server2mcp-starter-webmvc</artifactId>
    <version>1.2.0</version>
</dependency>
```

响应式（WebFlux）应用改用 `server2mcp-starter-webflux`，协议面完全一致。

---

## 延伸阅读

- 架构总览：`docs/reference/architecture.md`
- 扩展点：`docs/reference/extension-points.md`
- 权威事实源：项目根 `CLAUDE.md`
