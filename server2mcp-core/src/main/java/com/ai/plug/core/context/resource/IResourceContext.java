package com.ai.plug.core.context.resource;

import java.util.Set;

/** @header-start @module IResourceContext @keywords 资源上下文接口 getRawResources addResource 注册容器 极简接口 单一出口 @updated 2026-10-28 12:20 @layer core @author han @header-end */

/**
 * @author han
 * @time 2025/7/14 11:34
 */

public interface IResourceContext {
    /**
     * 获取 未处理的Resources
     */
    Set<String> getRawResources();

    /**
     * 添加 Resource
     * add Resource
     */
    void addResource(String name);
}
