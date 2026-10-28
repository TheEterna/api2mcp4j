package com.ai.plug.autoconfigure;

import com.ai.plug.autoconfigure.conditional.Conditions;
import com.ai.plug.autoconfigure.conditional.ParserNameValidator;
import com.ai.plug.autoconfigure.spec.AsyncSpecMcpConfig;
import com.ai.plug.autoconfigure.spec.SyncSpecMcpConfig;
import com.ai.plug.core.context.complete.CompleteContextFactory;
import com.ai.plug.core.context.complete.ICompleteContext;
import com.ai.plug.core.context.prompt.IPromptContext;
import com.ai.plug.core.context.prompt.PromptContextFactory;
import com.ai.plug.core.context.resource.IResourceContext;
import com.ai.plug.core.context.resource.ResourceContextFactory;
import com.ai.plug.core.context.root.IRootContext;
import com.ai.plug.core.context.root.RootContextFactory;
import com.ai.plug.core.context.tool.IToolContext;
import com.ai.plug.core.context.tool.ToolContextFactory;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * @header-start
 * @module Server2McpAutoConfiguration
 * @keywords 自动配置 入口 AutoConfiguration.imports Conditions.SystemCondition
 *           Server2McpAutoConfiguration Spring Boot 装配 MCP 服务端
 * @updated 2026-10-28 12:20
 * @purpose 框架自动配置入口，由 META-INF/spring 自动装配清单加载，串联注册链路与暴露链路
 * @layer autoconfigure
 * @affects 改动影响整个框架在用户应用中的装配时机与生效范围
 * @see McpConfig — 具体 Specification Bean 的装配体
 * @see ProtocolCoreAutoConfiguration — 协议层共享契约的单一装配源
 * @constraint 自动配置入口类：改动会波及所有使用本 starter 的下游应用
 * @author han
 * @header-end
 */

/**
 * @author 她也曾美如画
 * @time: 2025/04
 */
@Configuration
@Conditional(Conditions.SystemCondition.class)
@Import({
        McpConfig.class,
        AsyncSpecMcpConfig.class,
        SyncSpecMcpConfig.class
})
public class Server2McpAutoConfiguration {
    public static final Logger logger = LoggerFactory.getLogger(Server2McpAutoConfiguration.class);

    /**
     * 启动期解析器名校验器（注册纪律 R2）。
     *
     * <p>配置的解析器名拼错时，该解析器会静默不激活且零提示。此校验器在容器就绪后
     * 比对已知名集合，把这类错误变成启动期可见信号：默认 WARN 不阻断，
     * 设置 {@code plugin.mcp.parser.strict=true} 则拒绝启动。
     */
    @Bean
    public ParserNameValidator parserNameValidator(Environment environment) {
        return new ParserNameValidator(environment);
    }

}