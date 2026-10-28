package com.ai.plug.core.context.root;

/**
 * @header-start
 * @module RootContextFactory
 * @keywords 工厂模式 上下文工厂 createRootContext IRootContext 根目录容器 创建入口
 * @updated 2026-10-28 12:20
 * @purpose 以工厂方法创建根目录上下文实现，屏蔽具体实现类对外暴露
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/7/14 1:50
 */

public final class RootContextFactory {
    /**
     * create RootContext
     */
    public static IRootContext createRootContext() {
        return new RootContext();
    }
}
