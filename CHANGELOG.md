# Changelog

本文件记录 api2mcp4j（内部名称 server2mcp）的所有值得注意的变更。

格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

> **版本坐标**：`com.ai.plug:server2mcp-*`，Java 17+，Spring Boot 4.1.0 / Spring AI 2.0.0 / MCP Java SDK 2.0.0。

---

## [Unreleased]

尚未落地的规划项，见文末「路线图」。

---

## [1.2.0] - 2026-10-28

首个面向 Maven Central 发布的正式版本。1.1.x 期间为 `1.1.4-SNAPSHOT` 开发态，未对外发布。

### Added — 协议 2026-07-28 兼容层

MCP Java SDK 2.0 仅实现到协议 `2025-11-25`，本版本以自建 wire 层绕过 SDK 上限，同时保持 SDK 升级兼容（控制器在 SDK ≥ 3.0.0 落地时降级为 fallback）。

| 能力 | 说明 | 入口 |
|---|---|---|
| JSON-RPC 2.0 真路由 | 接管 MCP 协议 JSON-RPC 路由层 | `JsonRpcRouter` / `JsonRpcRoutes` · `POST /mcp/jsonrpc` |
| `server/discover` | 协议新握手方法 | `DiscoverEndpoint` |
| `tasks/*` | `tasks/create`·`get`·`list`·`cancel` + `tasks/augmented-prompt` | `TasksEndpoint` / `AugmentedPromptEndpoint` |
| `subscriptions/listen` | SSE 长连接形态，`Last-Event-ID` 断点续传 + 15s 心跳 | `SseNotificationsController` |
| MRTR | 多轮状态机会话、跨轮响应合并、8 轮安全护栏 | `MrtrDriver` / `MrtrToolCallbackWrapper` |
| ServerCapabilities wire schema | 2026-07-28 独立 wire 表达（含 `tools.subscription`、experimental tasks） | `WireServerCapabilities` / `WireSchemaExporter` |
| W3C traceparent | JSON-RPC 每条响应自动 mint + 透传 | `MetaUtils` |
| `_meta` 注入 | `resultType`·`ttlMs`·`cacheScope`·`cacheWrapperKey`·`taskHandle`·`inputRequests` | `McpCallToolResultConverter` / `McpResultWriter` |
| 分页 | `nextCursor` / `totalItems` / `PageList` 自动注入 | `McpPaging` / `PageCache` / `PaginatedLists` |
| 图标 | `Tool` Icons（SEP-973） | `McpToolProvider.buildIcons()` |
| ToolAnnotations | `audience` / `priority` / `lastModified` | `AbstractMcpToolMethodCallback` |
| `outputSchema` | 真实发送（此前已解析但丢弃） | `ToolDefinitionBuilder.buildToolOutputSchema()` |
| `structuredContent` | 结构化结果承载 | `DefaultMcpCallToolResultConverter` |
| Elicitation 双模式 | form / url 双形态 | `McpElicitation` |
| 幂等与重试 | `IdempotentCache` / `RetryPolicy` | `spec/dedup/` |
| 能力健康与变更通知 | 能力快照、健康报告、变更推送 | `spec/capabilities/` · `spec/change/` |

### Added — WebFlux starter 端点装配

`server2mcp-starter-webflux` 补齐协议 2026-07-28 全部端点，与 WebMVC **协议面完全对齐**；协议层共享 bean 下沉至 `server2mcp-autoconfigure`（`ProtocolCoreAutoConfiguration`），避免同一份契约在两个 starter 各建一套。

### Added — Maven Central 发布链路

- 根 pom 补齐 Central 强制元数据（name / description / url / license / developers / scm / issueManagement / distributionManagement）
- `release` profile 插件链：enforcer · source · javadoc · gpg · central-publishing 0.12.0
- `scripts/pre-release-check.sh`（8 组前置断言）· `scripts/publish-to-central.sh`（`--dry-run` / `--publish` 双模式）· `settings.xml.example` 凭据模板

### Added — AI 可读性基线

- 全仓 317 个 `.java` 文件补齐 `@header-start` / `@header-end` 文件头（`@module` / `@keywords` / `@updated` / `@layer`，L2+ 含 `@purpose`，L3 含 `@affects`）
- `scripts/check-file-header.mjs` 审计门禁：新增文件不合规即非零退出，含 `@layer` 与模块一致性断言

### Changed

- **依赖全量落到正式版**（此前为 SNAPSHOT）：Spring Boot `4.1.0` · Spring AI `2.0.0` · MCP Java SDK `2.0.0` · Jackson 3 · victools `5.0.0`；`java.version` 保持 17
- 版本号 `1.1.4-SNAPSHOT` → `1.2.0`，全模块对齐
- README / README_zh 版本坐标、parser 配置示例、WebFlux 模块说明、Roadmap 状态同步

### Fixed

- `McpToolScanConfigurer` 实现 `BeanDefinitionRegistryPostProcessor`（此前误为 `InitializingBean`，扫描配置在容器 refresh 时序中不生效）
- parser 配置键实为单数 `param` / `des`（文档曾误写 `params`），配置示例同步修正
- 根 pom 两个重复 `<compilerArgs>` 导致 `-parameters` 可能失效，已合并
- `maven-enforcer-plugin` 的 `requireProperty` 被误用于非 String 属性，已修正断言对象
- `McpElicitation` 生产代码路径中 3 处 `System.out.println("[DEBUG] …")` 已清零

### Security

- 移除仓库内 MySQL 爆破脚本与本地状态缓存
- Git 历史净化，敏感信息与公网 IP 一并脱敏
- 仓库凭据审计 0 命中；发布件（jar / sources / javadoc）内容审计 0 命中

### Deprecated / Removed

- 无公开 API 删除或重命名（本次为 minor 版本）

---

## 路线图

- [ ] R2 启动断言：对 `plugin.mcp.parser.param` / `.des` 做启动期拼写校验（方案已就绪，见 `docs/plan-R2启动断言-2026-10-28.md`，待批准）
- [ ] OTel SDK 真实埋点（当前仅 wire 格式）
- [ ] 多租户隔离（`@McpTool(tenant = "...")`）
- [ ] MCP Java SDK ≥ 3.0.0 原生路由迁移（`scripts/trigger-phase3.sh` 跟踪）

---

[Unreleased]: https://github.com/TheEterna/api2mcp4j/compare/v1.2.0...HEAD
[1.2.0]: https://github.com/TheEterna/api2mcp4j/releases/tag/v1.2.0
