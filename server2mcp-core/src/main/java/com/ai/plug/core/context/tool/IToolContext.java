package com.ai.plug.core.context.tool;

import java.util.Map;

/** @header-start @module IToolContext @keywords 工具上下文接口 getRawTools addTool ToolRegisterDefinition 注册容器 极简接口 单一出口 @updated 2026-10-28 12:20 @layer core @author han @header-end */

/**
 * @author han
 * @time 2025/7/14 11:34
 */

public interface IToolContext {
    /**
     * get raw tools
     */
    Map<String, ToolContext.ToolRegisterDefinition> getRawTools();

    /**
     * add tool
     */
    void addTool(String name, ToolContext.ToolRegisterDefinition tool);

}
