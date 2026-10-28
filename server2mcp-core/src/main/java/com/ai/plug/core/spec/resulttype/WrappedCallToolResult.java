package com.ai.plug.core.spec.resulttype;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * @header-start
 * @module WrappedCallToolResult
 * @keywords 结果包装 record sdkResult wireJson 线格式JSON 传输层出口
 *             协议扩展承载
 * @updated 2026-10-28 12:20
 * @purpose 把 SDK 结果与其线格式 JSON 成对承载，供传输层直接输出协议合规负载
 * @layer core
 * @author han
 * @header-end
 */

/**
 * Wrap of {@link McpSchema.CallToolResult} plus the wire-format JSON string
 * produced by {@link McpResultWriter#writeCallToolResultFromMeta(McpSchema.CallToolResult)}.
 * <p>
 * The standard {@code CallToolResult} is what the SDK passes between the
 * converter and the transport. {@code wireJson} carries the protocol 2026-07-28
 * extensions (resultType / ttlMs / cacheScope) that the SDK 2.0 record cannot
 * represent natively. Transports that want wire compliance serialize via
 * {@code wireJson} instead of running the SDK default serializer.
 *
 * @author han
 * @time 2026/8/1 05:49
 */
public record WrappedCallToolResult(McpSchema.CallToolResult sdkResult, String wireJson) {

    public WrappedCallToolResult {
        if (sdkResult == null) {
            throw new IllegalArgumentException("sdkResult is required");
        }
        if (wireJson == null) {
            throw new IllegalArgumentException("wireJson is required");
        }
    }
}