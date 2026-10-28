package com.ai.plug.core.spec.utils.root;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

/**
 * @header-start
 * @module McpAsyncRoot
 * @keywords roots 根列表 客户端根 异步实现 McpRoot Mono ListRootsResult
 *           McpAsyncServerExchange 客户端能力 资源根
 * @updated 2026-10-28 12:20
 * @purpose 异步列出客户端暴露的所有 roots，供工具方法按需访问
 * @layer core
 * @see McpRoot — roots 抽象接口
 * @see McpRootFactory — roots 实例工厂
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/7/11 15:46
 */

public class McpAsyncRoot implements McpRoot {

    private final McpAsyncServerExchange exchange;

    public McpAsyncRoot(McpAsyncServerExchange exchange) {
        this.exchange = exchange;
    }

    /**
     * 异步列出所有的根 async list all roots
     *
     * @return Mono<McpSchema.ListRootsResult>
     */
    @Override
    public Mono<McpSchema.ListRootsResult> listRootsAsync() {
        return exchange.listRoots();
    }

    /**
     * 同步列出所有的根 sync list all roots
     *
     * @return Mono<McpSchema.ListRootsResult>
     */
    @Override
    public McpSchema.ListRootsResult listRoots() {
        return this.listRootsAsync().block();
    }
}
