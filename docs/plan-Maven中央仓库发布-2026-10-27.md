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
P0 敏感信息与仓库卫生清理        ← ✅ 14:21 完成（commit fade220 / 3d97d33）
P1 发布元数据 + 插件链改造        ← ✅ 完成（commit b8a2b04）
P2 版本号 1.2.0 全模块对齐        ← ✅ 完成（同 commit）
P3 发布脚本 + 自检脚本 + 本地 verify 全绿 ← ✅ 14:57 完成（commit a26807e · 15 个发布件就绪）
P4 等待 R1/R2 凭据 → 真实上传      ← 🔴 阻塞：待董事长提供 Sonatype token + GPG 密钥
P5 R3 历史清理（需董事长单独批准 · 破坏性 git 写） ← 🔴 待批准
```

### 5.1 途中排掉的两个真实障碍（非代码问题）

| 障碍 | 根因 | 处置 |
|---|---|---|
| Maven 无法从 Central 下载**任何** artifact（连 guava 32.1.0-jre 这类必然存在的包都报 absent） | 环境有 `HTTPS_PROXY`，**curl 自动用它但 Java/Maven 不读**（Maven Resolver 只认 `settings.xml` 的 `<proxies>` 或 `https.proxyHost` 系统属性）→ JVM 直连得 404。此前 `mvn clean install` 能过纯属依赖全在本地缓存的巧合 | `~/.m2/settings.xml` 增 `<proxies>` 段（仓库外，不入库） |
| enforcer `requireProperty` 规则 3 失败 | `requireProperty` 只能断言 String 型属性，而 `project.license` / `project.developers` 是 List → 必然失败 | enforcer 只断言 `project.url` / `project.scm.url` / 版本 / Maven 版本；license / developers 完整性改由 `pre-release-check.sh` 以 XML 断言覆盖（职责分离） |

---

## 六、验收标准

| # | 判据 | 证据等级 | 状态 |
|---|---|---|---|
| 1 | `mvn clean install` 全绿（含 5 模块 + demo 单独构建） | E2 真库读数 | ✅ 608 测试 0 失败 + demo 单独构建 SUCCESS |
| 2 | `mvn -Prelease verify` 产出 5 artifact × (jar + sources + javadoc) | E2 | ✅ 15 个发布件全部就绪（14:57） |
| 3 | Central Portal 页面出现 `io.github.theeterna:server2mcp-starter-webmvc:1.2.0` | E3（外部可见） | 🔴 待 R1/R2 凭据 |
| 4 | 干净机器 `mvn dependency:get` 可拉取 | E2 | 🔴 待 R1/R2 凭据 |
| 5 | 发布件内无任何凭据（jar 内容审计） | E2 | ✅ 仅 Maven 标准 `pom.properties`，0 处密码/IP 命中 |
| 6 | 协议 2026-07-28 wire 兼容性未回归 | E2 端到端 | ✅ demo 实跑 21/21 断言通过 |

> 2026-10-28 更正：验收 #3/#4 的阻塞不止「凭据」——命名空间 `com.ai.plug` 本身无法由董事长认领。详见 §七。

---

## 七、2026-10-28 复核：发布路径上三处此前未发现的问题

> 触发：董事长答复「没 token」。借机把发布路径逐条对照 Sonatype 官方文档，发现此前「代码侧 100% 就绪，唯一阻塞是 token」的判断**不成立**。
> 核实方式：官方文档（WebFetch 抓取）+ 本仓代码 + 公共 DNS/GitHub 查询；每条标来源。

### 7.1 阻塞项（新）· 命名空间 `com.ai.plug` 无法由董事长认领 🔴

| 事实 | 来源 |
|---|---|
| 认领命名空间须证明拥有其反向域名。官方原话：`com.example` → 检查 `example.com`，「不检查 com.example.com 或任何其他变体」 | https://central.sonatype.org/register/namespace/ |
| `com.ai.plug` 的反向域名落在 `ai.com`（`plug.ai.com`）名下。官方页**未写明**三段式具体查哪一级，但无论哪一级都要求控制 `ai.com` 的 DNS | 同上 + 推断（推断部分未经 Sonatype 书面确认） |
| 本仓内无董事长持有 `ai.com` 的任何迹象。已知持有 `xiaohan.chat`（文档站 `docs.xiaohan.chat`，NS = `junade.ns.cloudflare.com` / `reza.ns.cloudflare.com`） | `scripts/deploy-cf-pages.sh`；dns.google DoH 实测 2026-10-28 |

**前提假设**：董事长不持有 `ai.com`。若实际持有，本节作废，方案 A 立即成立。

**对此前文档的更正**：§一「`com.ai.plug` 在 Central 未被占用」只证明**无人发布过**（search.maven.org numFound=0），不证明**可被认领**。

### 7.2 可选命名空间

| 方案 | groupId | 认领方式 | 评价 |
|---|---|---|---|
| **B（采用）** | `io.github.theeterna`（大小写以注册页实际显示为准） | 用 GitHub 账号 `TheEterna` 登录 Portal。官方：「Sonatype can, in most cases, automatically verify and provision publishing access to a namespace that looks like `io.github.<your GitHub username>`」；未自动开通则按页面指引或邮件 Central Support | 无需 DNS，约 5–10 分钟；坐标与仓库 URL 一致；仓库 `TheEterna/api2mcp4j` 已确认为 public、owner 即该账号 |
| C | `chat.xiaohan`（或其子段） | 在 Cloudflare 为 `xiaohan.chat` 加一条 DNS TXT（官方：「if you control example.com, you can use any groupId starting with com.example」） | 域名自有；坐标偏冷门 |
| A | 保持 `com.ai.plug` | 须控制 `ai.com` 的 DNS | 判定不可行 |

两点说明：
- **Java 包名 `com.ai.plug` 不动**，仅换 Maven groupId（二者本就无需一致）。因此 logger 名 `com.ai.plug: DEBUG`、源码路径 `com/ai/plug/**`、`import com.ai.plug.core.*` 全部不变。
- 此前从未发布到 Central → Central 上无既有下游；仅本地 `mvn install` 的使用者需改坐标。按 minor 处理，但必须写入 CHANGELOG。

### 7.3 配置缺陷（新）· token 是两段独立值，模板却共用一个变量 🔴

官方 Maven 发布页的 `settings.xml` 片段：`<username>` 与 `<password>` 分别是 token 的 username 与 password 两个独立值（来源：https://central.sonatype.org/publish/publish-portal-maven/ ）。

本仓 `settings.xml.example` 却把两处都写成 `${env.CENTRAL_PORTAL_TOKEN}`，`scripts/pre-release-check.sh` §7 与 `scripts/publish-to-central.sh` 头部注释也只认这一个变量 → 即使拿到 token，上传必然鉴权失败（username = password = 同一串）。

**修法**：拆为 `CENTRAL_PORTAL_USERNAME` / `CENTRAL_PORTAL_PASSWORD` 两个变量，三处同步。

### 7.4 配置缺陷（新）· `autoPublish=false` 与 `waitUntil=published` 自相矛盾 🟡

根 pom `central-publishing-maven-plugin` 当前配置 `<autoPublish>false</autoPublish>` + `<waitUntil>published</waitUntil>`（pom.xml:398-399）。官方页对 `waitUntil=published` 的描述为「This requires Automatic Publishing」。两项互相冲突；冲突时插件的具体行为官方页**未描述**（可能快速报错、可能空等 `waitMaxTime`=1800 秒、也可能放行）。

该路径只在 `deploy` 阶段触发，而此前所有验证（`mvn -Prelease clean verify`、`--dry-run`）都走不到它 → **从未被实测过**。

**修法**：`waitUntil` 改为 `validated`（官方默认值）。保留 `autoPublish=false` 不变：上传并通过校验后停在 Portal，由董事长到 https://central.sonatype.com/publishing/deployments 点 Publish——Maven Central 发布不可撤回，这一步人工闸应当保留。

### 7.5 另：此前一条记忆性结论无官方依据

此前记录「注册 Central 时必须用同一个 noreply 邮箱，否则 Central 校验 GPG UID 与账号邮箱不匹配会拒收」。官方 GPG 要求页（https://central.sonatype.org/publish/requirements/gpg/）**未见**任何 UID/邮箱须与账号或 POM 开发者邮箱一致的要求；仅要求公钥分发到 keyserver（支持 keyserver.ubuntu.com、keys.openpgp.org、pgp.mit.edu）并用主密钥签名。该说法已更正，不再作为约束。

### 7.6 groupId 迁移影响面（2026-10-28 实测，非估计）

**总计 32 处 / 12 个文件**：

| 类别 | 处数 | 文件 |
|---|---|---|
| pom.xml | 21 | `pom.xml`（自身 groupId + dependencyManagement×3）、`server2mcp-common/pom.xml`、`server2mcp-core/pom.xml`、`server2mcp-autoconfigure/pom.xml`、两个 starter 的 `pom.xml`、`server2mcp-test/pom.xml` |
| 非 pom 的 XML 坐标 | 8 | `README.md`×3、`README_zh.md`×3、`docs/reference/onboarding.md`×1、`scripts/verify-downstream-consumption.sh`×1 |
| 脚本内坐标与仓库路径 | 2 | `scripts/verify-downstream-consumption.sh`（printf 坐标、`~/.m2/repository/com/ai/plug/` 路径） |
| 文本坐标 | 1 | `CHANGELOG.md`（版本坐标说明） |

**明确不改**（非 Maven 坐标）：
- `server2mcp-test/src/main/resources/application.yml` 的 `com.ai.plug: DEBUG` —— 这是 logger 名 = **Java 包名**
- `scripts/verify-downstream-consumption.sh` 的 `import com.ai.plug.core.context.tool.IToolContext` —— **Java 包名**
- 全部 `com/ai/plug/**` 源码路径
- 历史日志 `docs/logs/2026-10-27_ceo_Maven发布与webflux补齐.md`、`docs/logs/2026-10-28_ceo_*.md` —— 记录的是当时事实，不回改

### 7.7 行动计划

| # | 动作 | 负责 | 前置 |
|---|---|---|---|
| 1 | 登录 https://central.sonatype.com（GitHub 账号 `TheEterna`）→ 确认/认领命名空间 → https://central.sonatype.com/usertoken 生成 User Token（**username + password 两段**，关闭弹窗后无法找回）→ 写入本机环境变量或 `~/.m2/settings.xml`，**不经聊天** | 董事长 | — |
| 2 | 回报页面上命名空间的原样字符串 | 董事长 | 1 |
| 3 | [RED] `pre-release-check.sh` 新增断言：settings 模板用两个变量；`central-publishing` 配置自洽（`autoPublish=false` 时 `waitUntil` 不得为 `published`）；全仓 groupId 一致 → 先红 | CEO | — |
| 4 | [GREEN] 改 groupId 与 32 处坐标引用；`waitUntil` 改 `validated`；拆 token 变量；同步 README×2 / CHANGELOG / onboarding / 验证脚本 / 发布脚本 / settings 模板 | CEO | 2 |
| 5 | `mvn clean install` + `scripts/verify-downstream-consumption.sh --run` + `scripts/publish-to-central.sh --dry-run` | CEO | 4 |
| 6 | `bash scripts/publish-to-central.sh --publish`，董事长亲自输入 `PUBLISH`，再到 Portal Deployments 页点 Publish | 董事长 | 5 |

---

> **维护者**：CEO（Han） · **董事长指令日**：2026-10-27 · **计划性质**：发布战役方案
