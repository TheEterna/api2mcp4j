package com.ai.plug.core.context.resource;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @header-start
 * @module ResourceContext
 * @keywords 资源上下文 注册容器 IResourceContext ConcurrentHashMap addResource getRawResources
 *           资源注册面 不可变视图 并发集合
 * @updated 2026-10-28 12:20
 * @purpose 以并发集合承载资源注册定义，是资源注册面到暴露面的唯一数据源
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * time: 2025/6/4 15:14
 */

public class ResourceContext implements IResourceContext{

    private static Set<String> rawResources = ConcurrentHashMap.newKeySet();

    ResourceContext() {
    }
    /**
     * 获取 未处理的Resources
     */
    @Override
    public Set<String> getRawResources() {
        return  Collections.unmodifiableSet(rawResources);
    }

    /**
     * 添加 Resource
     * add Resource
     */
    @Override
    public void addResource(String name) {
        rawResources.add(name);
    }



}
