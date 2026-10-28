package com.ai.plug.core.context.resource;

/**
 * @header-start
 * @module ResourceContextFactory
 * @keywords 工厂模式 上下文工厂 createResourceContext IResourceContext 资源容器 创建入口
 * @updated 2026-10-28 12:20
 * @purpose 以工厂方法创建资源上下文实现，屏蔽具体实现类对外暴露
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/7/14 16:00
 */

public class ResourceContextFactory {
    /**
     * create ResourceContext
     */
    public static IResourceContext createResourceContext() {
        return new ResourceContext();
    }
}
