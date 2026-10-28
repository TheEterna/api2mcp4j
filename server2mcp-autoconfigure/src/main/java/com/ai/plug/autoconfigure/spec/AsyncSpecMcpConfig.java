package com.ai.plug.autoconfigure.spec;

import com.ai.plug.autoconfigure.conditional.Conditions;
import com.ai.plug.core.builder.ToolDefinitionBuilder;
import com.ai.plug.core.context.complete.ICompleteContext;
import com.ai.plug.core.context.prompt.IPromptContext;
import com.ai.plug.core.context.resource.IResourceContext;
import com.ai.plug.core.context.root.IRootContext;
import com.ai.plug.core.context.tool.IToolContext;
import com.ai.plug.core.context.tool.ToolContext;
import com.ai.plug.core.springai.provider.AsyncMcpAnnotationProvider;
import io.modelcontextprotocol.server.McpServerFeatures;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ai.plug.common.constants.ConfigConstants.*;

/**
 * @header-start
 * @module AsyncSpecMcpConfig
 * @keywords 异步模式配置 IsAsyncCondition AsyncToolSpecification AsyncResourceSpecification
 *           AsyncPromptSpecification AsyncCompletionSpecification 条件装配 Reactive
 * @updated 2026-10-28 12:20
 * @purpose 在异步（Reactive）模式下装配四类 MCP 实体的异步 Specification Bean
 * @layer autoconfigure
 * @affects 改动影响 spring.ai.mcp.server.type=ASYNC 时的全部暴露面 Bean
 * @see SyncSpecMcpConfig — 同步模式的对应配置（两者条件互斥）
 * @constraint 双模式配置：与 SyncSpecMcpConfig 通过 Conditions 互斥激活，改动须保持对称
 * @author han
 * @header-end
 */

/**
 * Async MCP function configuration class
 * 异步 MCP 功能配置类
 * @author han
 * @time 2025/6/20 16:51
 */
@Configuration
@Conditional(Conditions.IsAsyncCondition.class)
public class AsyncSpecMcpConfig {


    @Configuration
    @Conditional(Conditions.ToolCondition.class)
    public static class AsyncToolSpecificationConfiguration {

        /**
         * 创建异步 Tool
         * Creates asynchronous Tool specifications
         * @param applicationContext spring容器
         * @return 异步的tools
         */
        @Bean
        @Primary
        public List<McpServerFeatures.AsyncToolSpecification> asyncToolSpecifications(
                ApplicationContext applicationContext,
                ToolDefinitionBuilder builder,
                IRootContext rootContext,
                IToolContext toolContext) {

            Map<Object, ToolContext.ToolRegisterDefinition> toolAndDefinitions = toolContext.getRawTools().entrySet().stream().collect(Collectors.toMap(
                    entry -> applicationContext.getBean(entry.getKey()),
                    Map.Entry::getValue
            ));
            return AsyncMcpAnnotationProvider.createAsyncToolSpecifications(toolAndDefinitions, builder, rootContext);
        }

    }

    @Configuration
    @Conditional(Conditions.ResourceCondition.class)
    public static class AsyncResourceSpecificationConfiguration {

        /**
         * 创建异步 资源(包括资源模板)
         * Creates asynchronous resources (including resource templates)
         * @param applicationContext spring容器
         * @return 异步的resources
         */
        @Bean
        @Primary
        public List<McpServerFeatures.AsyncResourceSpecification> asyncResourceSpecifications(
                ApplicationContext applicationContext,
                IResourceContext resourceContext
        ){
            List<Object> resources = resourceContext.getRawResources().stream().map(applicationContext::getBean).collect(Collectors.toList());
            // 通过sync转换成的async资源, 无需关注异步和同步的区别, 系统会自动转换
            return AsyncMcpAnnotationProvider.createAsyncResourceSpecifications(resources);

        }
    }


    @Configuration
    @Conditional(Conditions.PromptCondition.class)
    public static class AsyncPromptSpecificationConfiguration {
        /**
         * 创建异步 Prompt
         * Creates asynchronous Prompt specifications
         * @param applicationContext spring容器
         * @return 异步的prompts
         */
        @Bean
        @Primary
        public List<McpServerFeatures.AsyncPromptSpecification> asyncPromptSpecification(
                ApplicationContext applicationContext,
                IPromptContext promptContext){
            List<Object> prompts = promptContext.getRawPrompts().stream().map(applicationContext::getBean).toList();
            return AsyncMcpAnnotationProvider.createAsyncPromptSpecifications(prompts);
        }
    }

    @Configuration
    @Conditional(Conditions.CompleteCondition.class)
    public static class AsyncCompleteSpecificationConfiguration {

        /**
         * 创建异步 Completion
         * Creates asynchronous Completion specifications
         * @param applicationContext spring容器
         * @return 异步的completions
         */
        @Bean
        @Primary
        public List<McpServerFeatures.AsyncCompletionSpecification> asyncCompletionSpecification(
                ApplicationContext applicationContext,
                ICompleteContext completeContext){
            List<Object> completions = completeContext.getRawCompletes().stream().map(applicationContext::getBean).toList();
            return AsyncMcpAnnotationProvider.createAsyncCompletionSpecifications(completions);
        }

    }






}
