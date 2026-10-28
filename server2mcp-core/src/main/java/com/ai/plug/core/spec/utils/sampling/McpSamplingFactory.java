package com.ai.plug.core.spec.utils.sampling;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpSyncServerExchange;

/**
 * @header-start
 * @module McpSamplingFactory
 * @keywords 工厂模式 采样工厂 McpSampling McpSyncServerExchange McpAsyncServerExchange
 *           同步异步分派 类型路由 静态工厂 采样
 * @updated 2026-10-28 12:20
 * @purpose 按 exchange 实际类型产出采样实现，屏蔽同步/异步差异
 * @layer core
 * @see McpSampling — 被创建的抽象
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/7/8 18:12
 */

public class McpSamplingFactory {

    public static McpSampling getSampling(Object exchange) {

        if (exchange instanceof McpSyncServerExchange syncExchange) {
            return new McpSyncSampling(syncExchange);
        } else if (exchange instanceof McpAsyncServerExchange asyncExchange) {
            return new McpAsyncSampling(asyncExchange);
        }

        throw new IllegalArgumentException("Unsupported exchange type: " + exchange.getClass().getName()
        + "exchange must be McpSyncServerExchange or McpAsyncServerExchange");


    }
}
