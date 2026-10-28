package com.ai.plug.core.spec.utils.elicitation;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpSyncServerExchange;

/**
 * @header-start
 * @module McpElicitationFactory
 * @keywords 工厂方法 exchange类型分派 同步异步适配 getElicitation
 *             McpSyncElicitation McpAsyncElicitation
 * @updated 2026-10-28 12:20
 * @purpose 按 exchange 的同步异步类型分派出对应的 elicitation 实现
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/7/4 17:55
 */

public class McpElicitationFactory {

    /**
     * 获取一个McpElicitation对象
     * @param exchange McpServerExchange对象
     * @return
     */
    public static McpElicitation getElicitation(Object exchange) {

        if (exchange instanceof McpSyncServerExchange) {
            return new McpSyncElicitation((McpSyncServerExchange) exchange);
        } else if (exchange instanceof McpAsyncServerExchange) {
            return new McpAsyncElicitation((McpAsyncServerExchange) exchange);
        }
        // 不可能的情况
        // Impossible situation
        throw new IllegalArgumentException("exchange must be McpSyncServerExchange or McpAsyncServerExchange");
    }
}
