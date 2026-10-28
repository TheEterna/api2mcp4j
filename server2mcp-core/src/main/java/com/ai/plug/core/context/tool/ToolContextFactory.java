package com.ai.plug.core.context.tool;

/**
 * @header-start
 * @module ToolContextFactory
 * @keywords 工厂模式 上下文工厂 createToolContext IToolContext 工具容器 创建入口
 * @updated 2026-10-28 12:20
 * @purpose 以工厂方法创建工具上下文实现，屏蔽具体实现类对外暴露
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/7/14 16:01
 */

public class ToolContextFactory {
    /**
     * 创建 Tool 容器
     */
    public static IToolContext createToolContext() {
        return new ToolContext();
    }
}
