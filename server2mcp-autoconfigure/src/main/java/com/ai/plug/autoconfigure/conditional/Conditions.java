package com.ai.plug.autoconfigure.conditional;

import com.ai.plug.autoconfigure.*;
import com.ai.plug.core.parser.tool.des.AbstractDesParser;
import com.ai.plug.core.parser.tool.param.AbstractParamParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.server.common.autoconfigure.properties.*;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

import static com.ai.plug.common.constants.ConfigConstants.*;

/**
 * @header-start
 * @module Conditions
 * @keywords 条件判定 Condition 启动开关 SystemCondition IsInterfaceCondition IsSyncCondition
 *           IsAsyncCondition ParserCondition RootCondition ToolCondition ResourceCondition
 *           PromptCondition CompleteCondition 解析器类型装配 同步异步模式 分支激活 环境属性读取
 * @updated 2026-10-28 13:45
 * @purpose 承载插件全部条件判定类，按配置开关决定哪些 Bean 与解析器被激活装配
 * @layer autoconfigure
 * @affects 改动影响自动配置的整体装配面：总开关、作用域、同步异步模式以及每个解析器的注册与否
 * @see ConditionalOnParser — 解析器条件注解，绑定 ParserCondition
 * @constraint 解析器名拼写无启动校验（真实缺口 V-2），改动条件逻辑须同步 parser 名默认集合
 * @author han
 * @header-end
 */

/**
 * @author: han
 * time: 2025/04/2025/4/12 23:38
 * des:
 */
@Slf4j
public class Conditions {
    public static class SystemCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();


            return environment.getProperty(VARIABLE_PREFIX + "." + "enabled", Boolean.class, false);
        }
    }

    /**
     * 插件作用域为接口时才生效
     */
    public static class IsInterfaceCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();
            PluginProperties.ScopeType type = environment.getProperty(VARIABLE_PREFIX + "." + "scope", PluginProperties.ScopeType.class, PluginProperties.ScopeType.INTERFACE);
            return type.equals(PluginProperties.ScopeType.INTERFACE);
        }
    }
    public static class IsSyncCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();
            McpServerProperties.ApiType type = environment.getProperty(SPRING_AI_VARIABLE_PREFIX + "." + VARIABLE_TYPE, McpServerProperties.ApiType.class, McpServerProperties.ApiType.SYNC);
            return type.equals(McpServerProperties.ApiType.SYNC);
        }
    }
    public static class IsAsyncCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();
            McpServerProperties.ApiType type = environment.getProperty(SPRING_AI_VARIABLE_PREFIX + "." + VARIABLE_TYPE, McpServerProperties.ApiType.class, McpServerProperties.ApiType.SYNC);
            return type.equals(McpServerProperties.ApiType.ASYNC);
        }
    }


    public static class ParserCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            // 获取配置文件中的 parsers.types 属性
            Environment env = context.getEnvironment();
            Class<?> parserType = (Class<?>) metadata.getAnnotationAttributes(ConditionalOnParser.class.getName()).get("type");
            // 从注解元数据中获取要匹配的动物类型
            String parser = (String) metadata.getAnnotationAttributes(ConditionalOnParser.class.getName()).get("value");
            String[] types;


            if (ClassUtils.isAssignable(parserType, AbstractParamParser.class)) {
                types = env.getProperty(VARIABLE_PREFIX +'.' + PARSER_PREFIX + '.' + VARIABLE_PARAM, String[].class, new String[0]);
                types = types.length == 0 ? ParserNames.PARAM_NAMES.toArray(new String[0]) : types;

            }
            else if (ClassUtils.isAssignable(parserType, AbstractDesParser.class)) {
                types = env.getProperty(VARIABLE_PREFIX +'.' + PARSER_PREFIX + '.' + VARIABLE_DES, String[].class, new String[0]);
                types = types.length == 0 ? ParserNames.DES_NAMES.toArray(new String[0]) : types;
            } else {
                log.warn("not have the parserType");
                return false;
            }

            // 判断配置中是否包含该动物类型
            for (String type : types) {
                if (type.equalsIgnoreCase(parser)) {
                    return true;
                }
            }
            return false;
        }
    }


    public static class RootCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();
            return environment.getProperty(VARIABLE_PREFIX + VARIABLE_ROOT + VARIABLE_ENABLED, Boolean.class, true);
        }
    }
    public static class ToolCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();
            return environment.getProperty(VARIABLE_PREFIX + VARIABLE_TOOL + VARIABLE_ENABLED, Boolean.class, true);
        }
    }
    public static class ResourceCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();
            return environment.getProperty(VARIABLE_PREFIX + VARIABLE_RESOURCE + VARIABLE_ENABLED, Boolean.class, true);
        }
    }
    public static class PromptCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();
            return environment.getProperty(VARIABLE_PREFIX + VARIABLE_PROMPT + VARIABLE_ENABLED, Boolean.class, true);
        }
    }
    public static class CompleteCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();
            return environment.getProperty(VARIABLE_PREFIX + VARIABLE_COMPLETE + VARIABLE_ENABLED, Boolean.class, true);
        }
    }




}
