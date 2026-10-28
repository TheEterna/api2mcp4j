package com.ai.plug.core.spec.callback.tool;

import io.modelcontextprotocol.spec.McpSchema;

import java.lang.reflect.Type;

/** @header-start @module McpCallToolResultConverter @keywords 结果转换接口 convertToCallToolResult CallToolResult SPI 自定义转换器 极简接口 @updated 2026-10-28 12:20 @layer core @author han @header-end */

/**
 * @author han
 * @time 2025/6/27 8:24
 */

public interface McpCallToolResultConverter {
    McpSchema.CallToolResult convertToCallToolResult(Object result, Type returnType, AbstractMcpToolMethodCallback callback);
}
