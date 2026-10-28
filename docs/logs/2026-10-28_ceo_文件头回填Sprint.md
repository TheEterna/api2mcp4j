---
type: report
agent: CEO
task: 文件头回填 Sprint —— 全仓 317 个 Java 文件 AI 可读性基线 + 审计门禁 + 发布前文档债清理
date: 2026-10-28 12:40
duration: ~30min
related: docs/specs/FILE_HEADER_SPEC.md, scripts/check-file-header.mjs, scripts/verify-downstream-consumption.sh, docs/plan-文件头回填Sprint-2026-10-28.md
tags: 文件头, AI可读性, 审计门禁, 文档债, Maven发布, 下游验证
---

# 文件头回填 Sprint 闭环报告

## 摘要

董事长 2026-06-24 批准列为独立 Sprint 的「文件头回填」于今日完成：全仓 **317 个 `.java` 文件**（main 218 + test 99）100% 补齐 `@header-start`/`@header-end` 文件头，零逻辑行改动（Java 文件删除行 = 0、非注释新增行 = 0），并交付可复跑的审计门禁 `scripts/check-file-header.mjs`（当前 0 违规）。同期清理了积压约 4 个月的文档债（README 版本号停在 `1.1.4-SNAPSHOT`、参考文档仍称依赖是 SNAPSHOT、OutputSchema「当前不发送」、WebFlux「端点未装配」），新建 `CHANGELOG.md`，并**首次建立下游消费端到端验证**（干净工程依赖 `com.ai.plug:server2mcp-starter-webmvc:1.2.0` → 编译 → 启动 → 零 MCP 注解的 Controller 自动进入工具注册表，11/11 断言通过）。全量构建 `mvn clean install` 六模块 BUILD SUCCESS，**611 测试 0 失败 0 错误**，演示应用单独构建 SUCCESS。

---

## 详情

### 一、缺口量化

| 项 | 数据 |
|---|---|
| 全仓 `.java` 文件 | 317（main 218 + test 99） |
| 回填前缺 `@header-start` | **317（100%）** |
| 回填后不合规 | **0** |
| Java 文件删除行 | **0** |
| Java 文件非注释新增行 | **0** |

**为何 100% 缺口**：`FILE_HEADER_SPEC.md` §十 把「新增 `.java` 文件必须有文件头」列为 [ENFORCED]，但 2026-08-03 协议战役一次新增近百个 `core/spec/**` 文件且零合规 —— 规范已立、落地未跟上，CR 人工审查这道防线在批量新增时失效。

### 二、执行方式

| 批次 | 范围 | 承担 | 结果 |
|---|---|---|---|
| B0 | Plan + 审计脚本 | CEO 躬行 | `docs/plan-文件头回填Sprint-2026-10-28.md` · `scripts/check-file-header.mjs` |
| B1 | L3 核心引擎类 32 个 | **CEO 躬行** | 注册链路四件套 ×4 类型、解析器/回调抽象基类、Provider、自动配置、Builder |
| B2–B4 | L2/L1 共 285 个 | 2 名受控子 agent | 各 143 / 142，全部合规 |
| B5 | 全量验证 | CEO 躬行 | 611 测试全绿 + 审计 0 违规 |

**审计门禁的硬约束**（脚本内置断言）：
- `@header-start`/`@header-end` 齐备
- 必填字段 `@module` `@keywords` `@updated` `@layer` 无缺
- `@layer` ∈ {common, core, autoconfigure, starter, test}
- **`@layer` 与所在模块目录一致**（依赖流向铁律，CR 必查项）
- `@updated` 格式 `YYYY-MM-DD HH:mm`
- `@keywords` ≥ 10 字符（防占位敷衍）

### 三、判级纠偏

初稿把「具体 Context 实现 / Builder」误判为 L3。规范 §五 明文将二者归 L2，**以规范为准**已纠正，纠偏记录写入 Plan 文件。

### 四、同期清理的文档债（积压 4 个月）

| 文件 | 问题 | 处置 |
|---|---|---|
| `README.md` / `README_zh.md` | 依赖坐标写 `1.1.4-SNAPSHOT`（实际已 1.2.0） | 改 `1.2.0`；加 WebFlux starter 示例；parser `des` 示例补 `MCPTOOL`；工具命名示意 `orders_get_order` → `OrderController_getOrder`（真实规则）；Roadmap 勾选 WebFlux 与 Maven 进度 |
| `CHANGELOG.md` | **不存在** | 新建，含 1.2.0 全量变更（Added/Changed/Fixed/Security） |
| `docs/index.md` | 只索引到 08-03，缺 4 篇 10 月日志与 3 份 plan | 补全 |
| `docs/reference/architecture.md` | 描述的是 08-03 之前的架构，`core/spec/` 协议层（40 包/100+ 文件）几乎未提及 | 新增 §六「协议兼容层架构」（十二个能力域 + 与 starter 的关系） |
| `docs/reference/onboarding.md` | 坑 1 称依赖是 SNAPSHOT（07-30 已全落正式版）；坑 6 称 OutputSchema 不发送（08-03 已实装）；无协议层指引 | 坑 1 改写、坑 6 标记已修复、新增坑 7「协议新特性在 `core/spec/*` 不在 SDK 里」 |
| `scripts/pre-release-check.sh` | 无 CHANGELOG / README 版本一致性检查 | 新增第 9 组「文档版本一致性」 |

### 五、下游消费端到端验证（首次建立）

新增 `scripts/verify-downstream-consumption.sh`：构造一个**干净的外部 Maven 工程**（不属本仓、不在根 pom modules 内），依赖本地仓库的 `com.ai.plug:server2mcp-starter-webmvc:1.2.0`：

| 断言 | 结果 |
|---|---|
| 发布件及传递依赖可解析 | ✓ |
| 下游工程编译通过 | ✓ |
| 应用启动、`/mcp/discover` 可达 | ✓ |
| **零 MCP 注解的 `OrderController` 自动进入工具注册表** | ✓（注册表含 `orderController`） |
| **SDK 侧真实注册**（启动日志 `Registered tools: 2`） | ✓ |

**为什么需要**：本项目 CLAUDE.md 红线表明写「`mvn install` EXIT=0 只证明本仓不报错，证明不了下游用户不报错」。此前本仓无任何下游视角验证手段。

---

## 发现（意外发现 / 风险点 / 技术债）

### F1 · 多 agent 并发 Maven 构建导致 7 个假失败 🔴 已规避

首次 `mvn clean install` 报 7 个 `NoClassDefFound`（`McpServerCustomizersEndToEndTest$SpecHolder$1` 等）。根因：**两个子 agent 正在并发写同一 `target/` 目录**，编译产物不一致。单个测试类单跑通过、`clean install` 串行重跑 611 测试全绿，确认为假失败。

> **教训**：任何多 agent 场景下，Maven 构建必须串行；子 agent 的收尾验证应限定为不写 `target/` 的命令（如审计脚本、`git diff` 检查），构建统一由主控跑。

### F2 · 注册表 key 与暴露面工具名不是同一个东西 🟡 文档已补

- `IToolContext.getRawTools()` 的 key = **bean 名**（首字母小写的类名，如 `orderController`）—— 注册面是 **bean 粒度**
- MCP 客户端看到的工具名 = **`className_methodName`**（如 `OrderController_getOrder`）—— 暴露面是 **方法粒度**

CLAUDE.md 与 README 只描述了后者，未说明前者，易让人误判「注册表里找不到 = 没注册」。已在脚本注释与文档中补明。

### F3 · `interface` 作用域自动扫描链路无自动化测试覆盖 🔴 未修，待排期

grep 全仓测试：`IsInterfaceCondition`、`AutoConfiguredToolScannerRegistrar` **零命中**。而 `interface` 作用域正是本框架**非侵入式核心卖点**（现有 Controller 零改动即成 MCP 工具），当前仅靠本报告的下游脚本做人工验证，无 CI 保护。

> 建议补一个 `@SpringBootTest` 集成测试：定义一个零注解 `@RestController`，断言其方法进入 `IToolContext.getRawTools()`。

### F4 · `com.ai.plug` groupId 在 Maven Central 无坐标冲突 ✅

`search.maven.org` 查询 `g:"com.ai.plug"` → `numFound=0`。发布不存在被抢注风险。artifactId `server2mcp-*` 亦无冲突。

### F5 · 提交信息规范历史欠账 🟢 未修（不可逆）

`0c560d1 feat: dd`（2026-08-25）内容实为 VitePress 站点部署脚本 + `.gitignore` + demo 配置调整，commit message 无信息量，违反 Conventional Commits。改写历史属破坏性 git 操作（红线），未动。

---

## 结论与建议（按优先级）

1. **【待董事长批准】R2 启动断言** —— 方案已就绪：`docs/plan-R2启动断言-2026-10-28.md`。消解注册纪律 V-2 缺口（parser 名拼错 → 静默不激活 → 零告警）。已核实现场（`McpConfig.java:243/249/256/262/268/274` + `:285/292/299/305/312/318/325` 共 13 处 `@ConditionalOnParser`）。请示三事：① 是否批准动工 ② `plugin.mcp.parser.strict` 默认值取 `false`（默认 WARN 不阻断）是否认可 ③ 已知名权威源取 `ParserNames` 常量类 + 单测断言一致性是否认可
2. **【建议排期】补 `interface` 作用域集成测试** —— 见 F3，核心卖点缺 CI 保护
3. **【发布前置】Maven Central token** —— 代码侧 100% 就绪，`bash scripts/publish-to-central.sh --publish` 即可上传；唯一阻塞是 Sonatype Central Portal 账号与 token（注册须用 GPG UID 同一 noreply 邮箱）
4. **【已解决】R3 历史清理** —— 2026-10-27 已执行（`dbf2c9f`）

---

**MUST-WIN 验证（三源）**

| 源 | 证据 |
|---|---|
| E0 静态 | `node scripts/check-file-header.mjs` → 317 文件 0 违规；`git diff -- '*.java'` 删除行 0、非注释新增行 0 |
| E1 单测 | `mvn clean install` → 6 模块 SUCCESS，**611 测试 0 失败 0 错误 0 跳过**；`cd server2mcp-test && mvn clean package` → SUCCESS |
| E2 真库读数 | `bash scripts/verify-downstream-consumption.sh --run` → 11/11 通过（干净外部工程依赖 1.2.0 → 编译 → 启动 → 非侵入式注册生效 → SDK 侧 `Registered tools: 2`） |

---

**版本**：1.0 · 2026-10-28 · api2mcp4j Team
