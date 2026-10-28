package com.ai.plug.core.context.root;

import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;

/** @header-start @module IRootContext @keywords 根目录上下文接口 getRoots updateRoots setRoots 会话级roots 极简接口 生命周期管理 @updated 2026-10-28 12:20 @layer core @author han @header-end */

/**
 * @author han
 * @time 2025/7/14 1:51
 */

public interface IRootContext {

    /**
     * get Roots from exchange of the client
     * @param exchange
     * @return
     */
    List<McpSchema.Root> getRoots(Object exchange);

    /**
     * update Roots from exchange of the client
     * @param exchange
     * @param roots
     */
    void updateRoots(Object exchange, List<McpSchema.Root> roots);

    /**
     * set Roots from exchange of the client
     * @param exchange
     * @param roots
     */
    void setRoots(Object exchange, List<McpSchema.Root> roots);
}
