package com.ai.plug.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.ai.plug.common.constants.ConfigConstants.VARIABLE_PREFIX;

/**
 * @header-start
 * @module PluginProperties
 * @keywords 配置属性 plugin.mcp ConfigurationProperties 总开关 enabled 作用域 scope
 *           ScopeType 解析器枚举 DesParserType ParamParserType 解析器列表 parser des param
 *           自动配置绑定 实体开关 tool resource prompt complete root
 * @updated 2026-10-28 12:20
 * @purpose 承载 plugin.mcp.* 全部配置项，是插件总开关、作用域与解析器列表的唯一配置来源
 * @layer autoconfigure
 * @author han
 * @header-end
 */

/**
 * @author 韩
 */

@ConfigurationProperties(prefix = VARIABLE_PREFIX)
@Component
@Data
public class PluginProperties {

    private boolean enabled = false;

    private Parser parser;

    private Tool tool;

    private Resource resource;

    private Prompt prompt;
    private Root root;

    private Complete complete;

    /**
     * scope, 默认取值 interface 即为扫描接口
     */
    private ScopeType scope = ScopeType.INTERFACE;


    /**
     * 启动时扫描范围
     * INTERFACE 为 启动时 自动扫描注册接口工具
     * CUSTOM 为 啥也不干
     */
    public enum ScopeType {
        INTERFACE,
        CUSTOM;

    }

    /**
     * 工具描述解析器枚举
     * @Field TOOL
     * @Field JACKSON
     * @Field JAVADOC
     * @Field SWAGGER2
     * @Field SWAGGER3
     */
    public enum DesParserType {
        /**
         * 自创MCP专属注解
         */
        MCPTOOL,
        /**
         * Spring-ai 原生Tool系列注解解析
         */
        TOOL,
        /**
         * JACKSON系列注解解析
         */
        JACKSON,
        /**
         * JAVADOC解析, 需要特定条件,源码或 构建时同时构建源代码
         */
        JAVADOC,
        /**
         * SWAGGER2系列注解解析
         */
        SWAGGER2,
        /**
         * SWAGGER3系列注解解析
         */
        SWAGGER3
    }
    /**
     * 工具参数解析器枚举
     * @Field TOOL
     * @Field JACKSON
     * @Field JAVADOC
     * @Field SWAGGER2
     * @Field SWAGGER3
     */
    public enum ParamParserType {

        /**
         * 自创MCP专属注解
         */
        MCPTOOL,
        /**
         * Spring-ai 原生Tool系列注解解析
         */
        TOOL,
        /**
         * JACKSON系列注解解析
         */
        JACKSON,
        /**
         * SPRINGMVC系列注解解析
         */
        SPRINGMVC,
        /**
         * JAVADOC解析, 需要特定条件,源码或 构建时同时构建源代码
         */
        JAVADOC,
        /**
         * SWAGGER2系列注解解析
         */
        SWAGGER2,
        /**
         * SWAGGER3系列注解解析
         */
        SWAGGER3
    }

    @Data
    public static class Parser {
        private List<DesParserType> des;
        private List<ParamParserType> param;
    }

    @Data
    private static class Resource {
        private boolean enabled = true;
    }
    @Data
    private static class Prompt {
        private boolean enabled = true;
    }
    @Data
    private static class Tool {
        private boolean enabled = true;
    }
    @Data
    private static class Complete {
        private boolean enabled = true;
    }

    @Data
    private static class Root {
        private boolean enabled = true;
    }

}
