# Maven 中央仓库发布 · 实施方案

> 作者：CEO（Han） · 触发：董事长 2026-10-27 14:19 指令「然后给它放到 Maven 仓库里」
> 战役性质：对外发布（不可逆 · 红线场景 · 董事长已授权目标，凭据环节需董事长供给）
> 关联战役：`docs/plan-MCP协议全面集成-2026-07-30.md` Phase 1 / Phase 2

---

## 〇、给董事长的事实交代

### 0.1 本次发布的客观前提

| 事实 | 证据 | 影响 |
|------|------|------|
| 依赖已全落正式版 | 根 pom：Spring Boot 4.1.0 / Spring AI 2.0.0 / MCP SDK 2.0.0 / victools 5.0.0 | ✅ 无 SNAPSHOT 阻塞，可直接发布 |
| `com.ai.plug` groupId 在 Central 未被占用 | `search.maven.org` 查询 `g:com.ai.plug` → numFound=0 | ✅ 无命名冲突（仍需账号侧注册 namespace） |
| `server2mcp` 关键字无冲突 | 仅 `software.amazon.smithy.java:server-mcp:0.0.2` | ✅ |
| 本地无 Maven 凭据 | `~/.m2/settings.xml` **不存在** | 🔴 **阻塞发布执行** |
| 本地无 GPG 密钥 | `gpg --list-secret-keys` 空 | 🔴 **阻塞发布执行**（Central 强制签名） |
| 全量构建通过 | `mvn clean install -DskipTests` → EXIT=0 | ✅ 代码侧可发布 |

### 0.2 版本号决策

**`1.1.4-SNAPSHOT` → `1.2.0`**

理由（语义化版本 · minor bump）：2026-08-03 战役新增了 MRTR 多轮响应、tasks 生命周期、subscriptions/listen、JSON-RPC 路由层接管、`_meta` traceparent、Icons、structuredContent、ToolAnnotations、Pagination 分页、Elicitation 双模式等 **9 类对外可见能力** —— 属新增功能，非补丁级修复。
> 依据：`docs/logs/2026-08-03_ceo_protocol-2026-07-28-100pct-compat.md` §二 交付清单（8 个 commit）

---

## 一、发布物清单（5 个 artifact + 1 parent）

| artifactId | packaging | 说明 | 消费者 |
|---|---|---|---|
| `server2mcp-parent` | pom | 依赖管理 BOM + 插件配置 | 内部 / BOM 导入 |
| `server2mcp-common` | jar | 常量与工具类 | 内部 |
| `server2mcp-core` | jar | 核心引擎（解析器链 / 扫描器 / 回调 / Provider） | 内部 |
| `server2mcp-autoconfigure` | jar | Spring Boot 自动配置 | 内部 |
| `server2mcp-starter-webmvc` | jar | Spring MVC 接入 Starter | **对外主要入口** |
| `server2mcp-starter-webflux` | jar | Spring WebFlux 接入 Starter | **对外主要入口** |

**不发布**：`server2mcp-test`（演示应用，不在根 pom `<modules>` 内 · 依红线总表「不得当根 pom 模块构建」）。

---

## 二、POM 元数据补齐清单（Central 强制校验项）

| 项 | 现状 | 处置 |
|---|---|---|
| `name` | ✅ 已有 | 保留 + 各模块补 `<name>` / `<description>` |
| `description` | ❌ 缺失 | 补 |
| `url` | ❌ 缺失 | `https://github.com/TheEterna/api2mcp4j` |
| `license` | ❌ 缺失 | Apache-2.0（LICENSE 文件已在仓 · 11558 字节标准版） |
| `developers` | ❌ 缺失 | 补，邮箱用 GitHub noreply（**避免重蹈真实 QQ 邮箱泄漏覆辙**） |
| `scm` | ❌ 缺失 | git URL + connection/developer |
| `issueManagement` | ❌ 缺失 | GitHub Issues |
| `distributionManagement` | ❌ 缺失 | Sonatype Central Portal |
| 死属性 `<project.version>1.0.0` | ⚠️ 存在但无效 | 删除（实测 `${project.version}` 解析为内建值 1.1.4-SNAPSHOT，此 properties 被 Maven 模型属性屏蔽，删除无副作用） |

---

## 三、发布插件链（置于 `release` profile，默认构建不受影响）

| 插件 | 版本 | 职责 | 备注 |
|---|---|---|---|
| `maven-source-plugin` | 3.3.1 | 生成 `-sources.jar` | Central 强制 |
| `maven-javadoc-plugin` | 3.11.2 | 生成 `-javadoc.jar` | Central 强制；本项目中文 javadoc 需 `<doclint>none</doclint>` |
| `maven-gpg-plugin` | 3.2.7 | GPG 签名 | Central 强制；`gpg:sign-and-deploy-file` |
| `central-publishing-maven-plugin` | 0.8.0 | 上传至 Sonatype Central Portal | Central Portal 新标准（替代已停用的 OSSRH） |
| `maven-enforcer-plugin` | 3.5.0 | 发布前置断言（禁 SNAPSHOT / 禁 missing POM 元数据） | 防误发 |

---

## 四、🔴 红线项 · 需董事长供给（AI 无法自决）

| # | 事项 | 说明 | 影响 |
|---|---|---|---|
| R1 | **Sonatype Central Portal 账号 + token** | 需注册 `com.ai.plug` namespace；token 写入 `~/.m2/settings.xml` 的 `<server><id>central</id>` | 不提供 → 无法上传 |
| R2 | **GPG 密钥对** | Central 要求签名，且公钥须在 keyserver 可查（`keyserver.ubuntu.com`）；密钥 UID 邮箱须与 Central 账号已验证邮箱一致 | 不提供 → 无法上传 |
| R3 | **`git filter-repo` 销毁历史中的凭据** | commit `9c8be8b` / `687f19d` 含真实 MySQL 公网 IP `[REDACTED-MYSQL-HOST]` + 弱密码 `[REDACTED-MYSQL-PASSWORD]`；Central **不可删除、永久公开索引**，比 GitHub 更甚 | 不做 → 凭据随发布永久进入全球索引 |

**R3 补充事实**（2026-08-05 附录 C 已实证）：该主机为腾讯云北京 CVM（AS45090），**全部业务端口已关闭**（3306 已关），实例处于「到期停机未释放」状态 → 凭据本身当前不可利用，但 **IP + 库名 `yizi` + 用户名 `root` 的组合仍在公开可查**。

### 4.1 发布前 AI 侧全部可完成项（不依赖 R1/R2/R3）

1. 根 pom 元数据补齐 + 插件链 + enforcer
2. 版本号 `1.2.0` 全模块对齐（parent + 4 子模块 + demo）
3. `settings.xml.example` 模板落仓（不含真实凭据）
4. 发布脚本 `scripts/publish-to-central.sh`（含 dry-run 模式）
5. `mvn verify -Prelease` 本地全绿验证（签名步骤在无密钥时跳过）
6. 一键自检脚本 `scripts/pre-release-check.sh`

---

## 五、执行顺序（红线场景 · 董事长已授权目标）

```
P0 敏感信息与仓库卫生清理        ← 已于 14:21 执行
P1 发布元数据 + 插件链改造        ← 本计划主体
P2 版本号 1.2.0 全模块对齐
P3 发布脚本 + 自检脚本 + 本地 verify 全绿
P4 等待 R1/R2 凭据 → 真实上传
P5 R3 历史清理（需董事长单独批准 · 破坏性 git 写）
```

---

## 六、验收标准

| # | 判据 | 证据等级 |
|---|---|---|
| 1 | `mvn clean install` 全绿（含 5 模块 + demo 单独构建） | E2 真库读数 |
| 2 | `mvn -Prelease verify` 产出 6 artifact × (jar + sources + javadoc) | E2 |
| 3 | Central Portal 页面出现 `com.ai.plug:server2mcp-starter-webmvc:1.2.0` | E3（外部可见） |
| 4 | 干净机器 `mvn dependency:get` 可拉取 | E2 |
| 5 | 发布件内无任何凭据（jar 内容审计） | E2 |

---

> **维护者**：CEO（Han） · **董事长指令日**：2026-10-27 · **计划性质**：发布战役方案
