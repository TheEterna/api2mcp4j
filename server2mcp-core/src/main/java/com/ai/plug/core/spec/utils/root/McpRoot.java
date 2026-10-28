package com.ai.plug.core.spec.utils.root;

import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

/**
 * @header-start
 * @module McpRoot
 * @keywords roots 根列表 客户端根 roots/list 抽象接口 同步异步双形态
 *           ListRootsResult 客户端资源根
 * @updated 2026-10-28 12:20
 * @purpose 定义读取客户端 roots 的统一抽象，屏蔽同步/异步差异
 * @layer core
 * @see McpSyncRoot — 同步实现
 * @see McpAsyncRoot — 异步实现
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/7/11 15:45
 */

public interface McpRoot {

    /**
     * 异步列出所有的根 async list all roots
     * @return Mono<McpSchema.ListRootsResult>
     */
    Mono<McpSchema.ListRootsResult> listRootsAsync();

    /**
     * 同步列出所有的根 sync list all roots
     * @return Mono<McpSchema.ListRootsResult>
     */
    McpSchema.ListRootsResult listRoots();

}
