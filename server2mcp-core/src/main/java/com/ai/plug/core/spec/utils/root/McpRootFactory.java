package com.ai.plug.core.spec.utils.root;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpSyncServerExchange;

/**
 * @header-start
 * @module McpRootFactory
 * @keywords 工厂模式 roots工厂 McpRoot McpSyncServerExchange McpAsyncServerExchange
 *           同步异步分派 类型路由 静态工厂
 * @updated 2026-10-28 12:20
 * @purpose 按 exchange 实际类型产出 roots 实现，屏蔽同步/异步差异
 * @layer core
 * @see McpRoot — 被创建的抽象
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/7/11 15:46
 */

public class McpRootFactory {

    public static McpRoot getRoot(Object exchange) {

        if (exchange instanceof McpSyncServerExchange syncExchange) {
            return new McpSyncRoot(syncExchange);
        } else if (exchange instanceof McpAsyncServerExchange asyncExchange) {
            return new McpAsyncRoot(asyncExchange);
        }

        throw new IllegalArgumentException("Unsupported exchange type: " + exchange.getClass().getName()
                + "exchange must be McpSyncServerExchange or McpAsyncServerExchange");

    }

}
