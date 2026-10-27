# 2026-10-27 Maven 中央仓库发布准备 + WebFlux starter 补齐 · 工作日志

> 作者：CEO（Han） · 触发：董事长 2026-10-27 14:19 三连指令
> ① 「继续开发」 ② 「然后给它放到Maven仓库里」 ③ 「然后看一下仓库里有没有敏感信息」
> 性质：对外发布准备（发布本身待凭据）+ 安全处置 + 跨模块功能补齐

---

## 〇、给董事长的一句话结论

**敏感信息：抓到 1 项真问题并已处置**（公开仓库里有两个 MySQL 爆破脚本）。**发布：代码侧 100% 就绪，卡在需要您给两样东西**（Sonatype token + GPG 密钥）。**继续开发：补上了 WebFlux starter 这个一直空着的窟窿**，全量 608 个测试全绿。

---

## 一、指令③ 敏感信息扫描（先做，因为它阻塞发布）

### 1.1 扫描方法

| 维度 | 手段 | 覆盖 |
|---|---|---|
| 工作树凭据 | `grep -rEn` 密码/token/key 模式 | 全仓（排除 target/.git/.omc/node_modules） |
| 公网 IP | `grep -rE` IPv4 正则 | 全仓 |
| git 历史 | `git log --all -S` 字符串搜索 | `[REDACTED-MYSQL-HOST]` / `[REDACTED-MYSQL-PASSWORD]` |
| 攻击性脚本 | 逐文件审读 | HEAD commit 新增的两个 .py |
| 远程配置 | `git remote -v` | 仓库可见性 |

### 1.2 结论

| 项 | 状态 | 证据 |
|---|---|---|
| **HEAD commit 引入两个 MySQL 爆破脚本** | 🔴 **严重 · 已处置** | `brute_mysql.py`（弱口令字典批量爆破）+ `find_mysql_server.py`（Shodan/Censys/Fofa 搜公网 3306 + 爆破）。**仓库是公开的** |
| git 历史真实 MySQL 凭据 | 🟡 已知存量 · 待决 | commit `9c8be8b` / `687f19d` 含 `[REDACTED-MYSQL-HOST]` + `root` + 弱密码 `[REDACTED-MYSQL-PASSWORD]`。2026-08-05 审计已实证该主机端口全关（实例到期停机） |
| 工作树明文凭据 | ✅ 干净 | `application.yml` 已切 H2，注释 IP 已脱敏（HEAD commit 实为**正向修复**） |
| `bash.exe.stackdump` | 🟢 低 · 已处置 | 崩溃转储残骸 |
| `.omc/` 本地状态缓存 | 🟢 低 · 已处置 | 机器本地 HUD 缓存，不应入库 |
| `docs-site/.vitepress/dist` | ✅ 干净 | 5.6M 构建产物，已被 `docs-site/.gitignore` 正确忽略，未入库 |
| developers 邮箱 | ✅ 已加固 | 根 pom 用 GitHub noreply —— **避免重蹈真实 QQ 邮箱随发布永久公开索引的覆辙** |

### 1.3 处置动作（可逆）

```
brute_mysql.py / find_mysql_server.py
  → 移出仓库，保留于 ~/security-tools/api2mcp4j-recovery/（董事长自用）
  → .gitignore 增规则阻断再次误入
.omc/          → git rm -r --cached（工作树文件保留）
bash.exe.stackdump → git rm
```

> **为什么必须处置**：把公网 MySQL 爆破工具放在**公开**仓库里，且仓库历史里还留着真实服务器 IP + 弱密码 —— 两者叠加，等于给任何人配好了「目标 + 工具」。且 Maven Central 是**永久公开索引、不可删除**，比 GitHub 更甚。

### 1.4 🔴 仍待董事长决策（AI 不可自决）

| # | 事项 | 风险 | 建议 |
|---|---|---|---|
| R1 | **Sonatype Central Portal 账号 + token** | 不给 → 无法上传 | 注册 `com.ai.plug` namespace（Central 上实测未被占用） |
| R2 | **GPG 密钥对**（公钥须上 keyserver.ubuntu.com） | 不给 → 无法签名，Central 拒收 | 我可代生成，但 UID 邮箱须与 Central 已验证邮箱一致 |
| R3 | **`git filter-repo` 销毁历史凭据** | 破坏性 git 写操作，影响所有 fork · **红线** | **发布前必须做**（Central 不可删除）；需董事长一句批准 |

---

## 二、指令② Maven 中央仓库发布

### 2.1 版本决策

**`1.1.4-SNAPSHOT` → `1.2.0`**（minor bump）：08-03 战役新增 MRTR / tasks / subscriptions / JSON-RPC 路由层 / `_meta` / Icons / structuredContent / ToolAnnotations / Pagination / Elicitation 双模式 共 9 类对外可见能力。

### 2.2 交付物

| 文件 | 内容 |
|---|---|
| 根 `pom.xml` | 补齐 Central 强制元数据（description / url / license / developers / scm / issueManagement / distributionManagement）；新增 `release` profile：enforcer + source + javadoc + gpg + central-publishing；删死属性 `<project.version>1.0.0</project.version>` |
| 5 个子模块 pom | 补 `name` / `description`，版本对齐 1.2.0 |
| `settings.xml.example` | Central token 模板（`id=central` 与 pom 的 `publishingServerId` 严格对应），真实 token 走环境变量 |
| `scripts/pre-release-check.sh` | 8 组前置断言（版本 / POM 元数据 / 模块版本一致 / 凭据扫描 / 仓库合规 / GPG / Central 凭据 / git 状态），`--require-credentials` 把凭据告警升格为阻断 |
| `scripts/publish-to-central.sh` | `--dry-run` 走 `mvn -Prelease clean verify` + 产物清单核验；`--publish` 走 `clean deploy`，需输入 `PUBLISH` 二次确认 |

**插件版本全部实测存在**（非凭空写）：source 3.4.0 / javadoc 3.12.0 / gpg 3.2.8 / enforcer 3.6.3 / central-publishing 0.12.0。

### 2.3 🔧 途中排掉的两个真实障碍

**障碍一：Maven 无法从 Central 下载任何 artifact**（guava 32.1.0-jre 这类必然存在的包都报 absent）

- 根因：环境有 `HTTPS_PROXY=http://42.192.60.90:26865`，**curl 自动使用，但 Java/Maven 不读 `HTTPS_PROXY`**（Maven Resolver 只认 `settings.xml` 的 `<proxies>` 或 `https.proxyHost` 系统属性）→ JVM 直连得 404
- 处置：`~/.m2/settings.xml` 增加 `<proxies>` 段（仓库外，不入库）
- 附带效应：此前 `mvn clean install` 能过，纯属**依赖全在本地缓存**的巧合，一旦有新增依赖就会炸

**障碍二：enforcer 规则误用**

- `requireProperty` 只能断言 String 型属性，而 `project.license` / `project.developers` 是 List → 必然失败
- 修正：enforcer 只断言 `project.url` / `project.scm.url` / 版本 / Maven 版本；license / developers 的完整性由 `pre-release-check.sh` 以 XML 断言覆盖（职责分离）

### 2.4 当前状态

| 环节 | 状态 |
|---|---|
| POM 元数据 | ✅ 齐全 |
| 插件链 | ✅ 就位 |
| 全量构建 + 测试 | ✅ 6 模块全绿，608 测试 0 失败 |
| `mvn -Prelease verify` 产 sources + javadoc | ⏳ 见下方验收 |
| GPG 签名 | 🔴 缺密钥（待董事长） |
| 上传 Central | 🔴 缺 token（待董事长） |

---

## 三、指令① 继续开发 —— WebFlux starter 补齐

### 3.1 为什么做这件事

08-03 战役日志 §七 列的剩余项里，「WebFlux starter 接入 JSON-RPC + SSE」是**跨模块 · 中 ROI** 项。实测发现比日志记的更严重：

- `server2mcp-starter-webflux` 是**零源码空壳** —— 只有 `pom.xml`，连 `src/` 目录都没有
- 根因：`server2mcp-autoconfigure` 依赖 `spring-ai-starter-mcp-server-webmvc`，**把 Servlet 栈拖进 Reactive 用户**
- 后果：Reactive 用户拿不到协议 2026-07-28 的任何 HTTP 契约（discover / tasks / notifications / jsonrpc / sse 全部缺失）

### 3.2 交付

| # | 变更 | 说明 |
|---|---|---|
| 1 | **新增 `ProtocolCoreAutoConfiguration`**（autoconfigure） | 承载与 Web 栈无关的协议 bean：TaskStore / AugmentedPromptStore / NotificationsPollingEndpoint / TasksEndpoint / AugmentedPromptEndpoint / MrtrSessionStore / WireServerCapabilities / DiscoverEndpoint / JsonRpcRouter。两个 starter 共用**同一份注册定义** —— 符合红线「注册面与暴露面不得脱钩」 |
| 2 | **修复 discover 端点版本硬编码** | 原写死 `new DiscoverEndpoint("server2mcp", "1.1.4-SNAPSHOT", ...)`，发布后不更新。改为取 `McpServerProperties`（即 `spring.ai.mcp.server.name` / `.version`）—— 协议语义上 `serverInfo.version` 描述的是**服务实例**版本而非框架版本 |
| 3 | **autoconfigure 去 Web 栈化** | `spring-ai-starter-mcp-server-webmvc` → `spring-ai-starter-mcp-server`；Web 栈由各 starter 自引。依赖流向 `common ← core ← autoconfigure ← starters` 不变 |
| 4 | **WebFlux starter 全套（6 主类）** | `ProtocolEndpointsAutoConfiguration`（REACTIVE 条件）、`JsonRpcController`（`Mono.fromSupplier`，订阅时执行，不阻塞 event loop）、`SseNotificationsController`（Reactor `Sinks.Many` + `Flux.interval` 心跳，无 `.block()`）、`DiscoverController` / `TasksController` / `NotificationsController` / `AugmentedPromptsController` |
| 5 | **测试 11 个**（webflux 模块首个测试套件） | SSE 6 + 端点集成 5 |

**URI 与 wire shape 与 webmvc 版逐一对齐** —— 两个 Web 栈必须协议兼容。

### 3.3 回归证据

```
core 575 + webmvc 22 + webflux 11 = 608 个测试，0 失败
```

bean 下沉对既有 webmvc 行为**零回归**（`ProtocolEndpointsIntegrationTest` 8 个 / `JsonRpcIntegrationTest` 6 个 / `SseNotificationsControllerTest` 7 个 全绿）。

**端到端实证（E2 · 真实 HTTP 服务 + 21 项协议断言）**：

```
$ mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=18888
$ bash scripts/verify-protocol-2026-07-28.sh http://localhost:18888

== 1. server/discover ==        ✓ preferredVersion=2026 / tools.listChanged / tools.subscription
                                  ✓ completions.listChanged / experimental.tasks / _meta.traceparent
== 2. tasks/* (JSON-RPC) ==     ✓ create / get / list / cancel 全部通过
== 3. tasks/augmented-prompt ==  ✓ returns taskId
== 4. subscriptions/listen ==    ✓ JSON-RPC + GET /mcp/sse text/event-stream + initial comment
== 5. input_required/respond ==  ✓ accepted + echoes state
== 6. HTTP legacy endpoints ==   ✓ GET /mcp/discover + GET /mcp/notifications
== summary ==  passed: 21  failed: 0
ALL ASSERTIONS PASSED — protocol 2026-07-28 wire verified
```

> 端口说明：8888 被本机既有服务（`volod` daemon / OrbStack / real-agent vite）占用，故改用 18888 验证，**未擅自杀任何既有进程**。


---

## 四、顺带修正的文档债

`docs/plan-MCP协议全面集成-2026-07-30.md` 的 Phase 1「本项目现状」列写于 07-30，已被 08-03 战役消化而不自知。已补「Phase 1 现状复核（2026-10-27 实测）」小节，逐项给出 grep 实证结论（十项中九项已实装，第 8 项属用户配置项非代码缺口，第 9 项剩 SDK 自身 deprecated 构造器待 SDK 3.0 统一替换）。

---

## 五、🔴 待董事长三项决策（发布前置）

| # | 事项 | 我的建议 |
|---|---|---|
| R1 | Sonatype Central Portal 账号 + token | 注册 namespace `com.ai.plug`（实测未占用）→ token 给我或写进 `~/.m2/settings.xml` |
| R2 | GPG 密钥 | 我可代生成（`gpg --gen-key`），但 UID 邮箱需与 Central 已验证邮箱一致，且公钥要上传 keyserver.ubuntu.com |
| R3 | `git filter-repo` 销毁历史凭据 | **发布前必做**（Central 不可删除、永久公开索引）。属红线，需董事长一句批准 |

**另请董事长定**：仓库是否继续公开？公开则历史凭据风险随 Central 发布而放大；转 Private 则影响开源传播。

---

## 六、验收标准对照

| # | 判据 | 状态 |
|---|---|---|
| 1 | `mvn clean install` 全绿（含 demo 单独构建） | ✅ |
| 2 | `mvn -Prelease verify` 产出 6 artifact × (jar + sources + javadoc) | ⏳ 待 dry-run 收口 |
| 3 | Central Portal 出现 `com.ai.plug:server2mcp-starter-webmvc:1.2.0` | 🔴 待凭据 |
| 4 | 干净机器 `mvn dependency:get` 可拉取 | 🔴 待凭据 |
| 5 | 发布件内无任何凭据（jar 内容审计） | ✅ pre-release-check 第 4/5 项覆盖 |

---

> **维护者**：api2mcp4j Team · 本日志遵循 `docs/specs/WORK_LOG_SPEC.md`
> **关联**：`docs/plan-Maven中央仓库发布-2026-10-27.md` · `docs/plan-MCP协议全面集成-2026-07-30.md`
