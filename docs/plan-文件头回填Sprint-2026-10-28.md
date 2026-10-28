# 文件头回填 Sprint（第一批：全仓 AI 可读性基线）

> 立宪日：2026-10-28 · 维护者：CEO（Han） · 授权来源：董事长 2026-06-24 批准「文件头回填列为后续独立 Sprint」· 规范正本：`docs/specs/FILE_HEADER_SPEC.md`
> 红线判定：**非红线**（不改逻辑、不动公开 API、不改依赖流向）→ CEO 自决执行 + 事后简报

---

## 一、背景与问题量化

| 项 | 数据 | 来源 |
|---|---|---|
| 全仓 `.java` 文件数 | **317**（main 218 + test 99） | `find . -name "*.java" -not -path "*/target/*"` |
| 缺 `@header-start` 的文件数 | **317（100%）** | 同上，逐文件 `head -30 \| grep` |
| 规范要求 | 新增文件**强制**带文件头（[ENFORCED]） | `FILE_HEADER_SPEC.md` §十 |
| 规范现状记载 | "当前所有 .java 文件均无 @header-start" | `FILE_HEADER_SPEC.md` §九（2026-06-24 勘察） |
| 08-03 协议战役新增 | `core/spec/**` 约 40 包、近百文件全部零文件头 | `docs/logs/2026-08-03_ceo_protocol-2026-07-28-100pct-compat.md` |

**问题本质**：`FILE_HEADER_SPEC` §十 已把"新增文件必须有文件头"列为 [ENFORCED]，但 08-03 协议战役一次新增近百个文件且零合规 → **规范已立、落地未跟上**，CR 人工审查这一道防线在批量新增时失效。这是注册纪律「强制注册」思想在文档层的同型缺口：**规范说了，但没有可验证的门禁**。

**本 Sprint 的双目标**：
1. **补齐存量** —— 317 个文件全部带上合规文件头
2. **补上门禁** —— 交付可复跑的审计脚本 `scripts/check-file-header.mjs`，让"以后每次新增文件合规"从人工抽查变成机器断言

---

## 二、范围与判级

### 2.1 覆盖范围（全量，不搞"渐进式"）

`FILE_HEADER_SPEC` §九 原文建议"渐进式补加、不搞一次性全量改造"（理由：避免大面积无意义 diff）。**本 Sprint 偏离该建议，理由如下**：

1. 董事长已明确批准将其列为**独立 Sprint**（一次性做完），非渐进式补加
2. 08-03 战役新增近百文件已使"渐进式"失效——待补存量已达历史峰值，继续拖越大
3. 本 Sprint **只加文件头、不改任何逻辑行**，diff 体积大但语义零风险
4. 交付审计脚本后，"渐进式"由脚本接管：此后新增文件不合规 → 脚本非零退出

### 2.2 判级映射（依 §五「判级速记」）

| Level | 判据 | 本仓对应 | 必填字段 |
|---|---|---|---|
| **L3 完整** | 改它牵动整条注册/解析链 | 注册链路四件套（`Mcp*ScanRegistrar`/`Mcp*ScanConfigurer`/`ClassPath*Scanner` ×4 类型）、解析器抽象基类（`AbstractDesParser`/`AbstractParamParser`）、回调抽象基类（`AbstractMcp*MethodCallback` ×4）、Provider（6）、自动配置（`Server2McpAutoConfiguration`/`ProtocolCoreAutoConfiguration`/`McpConfig`）、双模式配置（`AsyncSpecMcpConfig`/`SyncSpecMcpConfig`） | + `@purpose` + `@affects` + `@see`/`@constraint` |
| **L2 标准** | 链路中可替换的一环 | 具体解析器（des 6 / param 8）、**具体 Context 实现**、ContextFactory、Builder、`*Converter`、Controller、`PluginProperties`、工具类 | + `@purpose` |
| **L1 单行** | 数据载体 / 纯声明 | 注解定义（13）、常量类（4）、POJO/DTO、**`I{Type}Context` 极简接口** | `@module` + `@keywords` + `@updated` + `@layer` |

> **判级纠偏记录**（2026-10-28）：初稿曾把「具体 Context 实现 / Builder」误判为 L3。规范 §五 明文归 L2（「具体 Context 实现、Builder」列于 L2 行），**以规范为准**，本 Sprint 按上表执行。`来源：docs/specs/FILE_HEADER_SPEC.md §五`

### 2.3 `@layer` 取值与模块映射（依 §六 五层枚举）

| 模块目录 | `@layer` |
|---|---|
| `server2mcp-common/**` | `common` |
| `server2mcp-core/**` | `core` |
| `server2mcp-autoconfigure/**` | `autoconfigure` |
| `server2mcp-spring-boot-starters/**` | `starter` |
| 任一模块 `src/test/**` | `test` |
| `server2mcp-test/**`（演示应用） | `test` —— **见 §六 待确认项** |

---

## 三、执行批次

| 批次 | 范围 | 文件数 | 承担方式 | 验收 |
|---|---|---|---|---|
| **B0** | 审计脚本 + Plan | 2 | CEO 躬行 | 脚本对"零文件头"现状报 317 命中；对人工造合规样本报 0 命中 |
| **B1** | L3 核心引擎类 | ~40 | CEO 躬行 | 抽查 5 个：L3 含 `@purpose`+`@affects`，`@layer` 与模块一致 |
| **B2** | L2 链路环节类 | ~90 | 子 agent 批处理（受控提示词 + 逐文件模板） | CEO 抽查 10 个 + 全量脚本 |
| **B3** | L1 注解/常量/POJO/接口 | ~90 | 子 agent 批处理 | CEO 抽查 10 个 + 全量脚本 |
| **B4** | test 类 + starter 端点 + 演示应用 | ~95 | 子 agent 批处理 | 全量脚本 + 构建 |
| **B5** | 全量验证 | — | CEO 躬行 | `mvn clean install` 全绿（608 测试 0 失败）+ `cd server2mcp-test && mvn clean package` SUCCESS + 审计脚本 0 命中 |

> 批次边界选择：同一 commit 内只做"加文件头"一件事，**不夹带任何逻辑改动**——保证 diff 可用 `git diff -w` 验证为纯注释变更。

---

## 四、交付物

1. `docs/plan-文件头回填Sprint-2026-10-28.md`（本文件）
2. `scripts/check-file-header.mjs` —— 审计脚本，机器断言以下硬约束：
   - 每个 `.java`（排除 `target/`）含 `@header-start` 与 `@header-end`
   - 必填字段齐备：`@module` `@keywords` `@updated` `@layer`
   - `@layer` ∈ {common, core, autoconfigure, starter, test}
   - `@layer` 与所在模块目录一致（依赖流向铁律，CR 必查项）
   - `@updated` 格式 `YYYY-MM-DD HH:mm`
   - `@keywords` 非空且 ≥ 10 字符（防占位敷衍）
   - 退出码：0 = 全绿；1 = 有违规（打印清单）
3. 317 个文件的文件头回填
4. `docs/logs/2026-10-28_ceo_文件头回填Sprint.md` —— 工作留痕

---

## 五、验收标准

| # | 判据 | 证据等级 | 验证方式 |
|---|---|---|---|
| 1 | 317 个 Java 文件全部带 `@header-start`/`@header-end` | E0 静态检查 | `node scripts/check-file-header.mjs` → 0 违规 |
| 2 | `@layer` 与所在模块 100% 一致 | E0 | 同上（脚本内置模块映射断言） |
| 3 | L3 文件含 `@purpose` + `@affects` | E1 人工抽查 5 例 | CEO 抽查 |
| 4 | 无任何逻辑行变更 | E0 | `git diff -U0 \| grep -v "^[+-] *\*" \| grep "^[+-]"` → 仅文件头注释行 |
| 5 | 全量构建通过 | E2 真库读数 | `mvn clean install` → 608 测试 0 失败；`cd server2mcp-test && mvn clean package` → SUCCESS |

---

## 六、待董事长确认项（不阻塞本 Sprint 执行）

| # | 事项 | 我的建议 | 依据 |
|---|---|---|---|
| 1 | `server2mcp-test` 演示应用主代码的 `@layer` 取值 | 暂标 `test` | 规范 §六 五层枚举无 `demo` 层；新增枚举属规范实质变更，须批准 |
| 2 | **R2 启动断言（注册纪律 V-2 缺口）** 是否批准动工 | **建议批准** | `REGISTRATION_DISCIPLINE_SPEC.md` §6.5 明写「属全局规范/协议层改动，动工前向董事长申请」。现状：`plugin.mcp.parser.param/des` 拼错 → `Conditions.ParserCondition` 静默不激活 → 启动零告警 → 运行时才发现描述缺失 |

---

## 七、风险与回滚

| 风险 | 概率 | 影响 | 缓解 |
|---|---|---|---|
| 文件头与实际行为不符（描述漂移） | 中 | AI 被误导 | 判级由 CEO 亲自做（B1），子 agent 产出 100% 由 CEO 抽查 + 脚本断言；`@purpose` 措辞取自类实际职责而非类名直译 |
| 误改逻辑行 | 低 | 破坏功能 | B5 判据 #4 用 `git diff` 机械验证"仅注释行变更"；全量构建 + 608 测试兜底 |
| 大 diff 影响 `git blame` | 低 | 追溯变难 | 文件头**加在既有 Javadoc 上方**（保留原 `@author`/`@time`/`des:`），不覆盖原注释 |

**回滚成本**：单批次 `git revert <commit>`，无逻辑改动 → 回滚零风险。

---

**版本**：1.0 · 2026-10-28 · api2mcp4j Team
