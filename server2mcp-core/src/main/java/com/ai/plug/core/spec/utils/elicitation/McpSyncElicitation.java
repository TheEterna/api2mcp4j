package com.ai.plug.core.spec.utils.elicitation;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.*;

/**
 * @header-start
 * @module McpSyncElicitation
 * @keywords elicitation 同步实现 McpSyncServerExchange ElicitFormRequest
 *               客户端表单收集 boundedElastic 异步包装
 * @updated 2026-10-28 12:20
 * @purpose 以同步 exchange 发起 elicitation 请求，并把同步调用包装成 Mono 适配异步链路
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/7/4 17:40
 */

public class McpSyncElicitation implements McpElicitation{


    private final McpSyncServerExchange exchange;

    public McpSyncElicitation(McpSyncServerExchange exchange) {
        this.exchange = exchange;
    }

    /**
     * 通过 exchange 向 客户端 发起elicitation 请求, 指定反序列化的 格式
     *
     * @param message 请求的消息
     * @param schema  反序列化的格式
     * @return McpSchema.ElicitResult
     */
    @Override
    public McpSchema.ElicitResult elicit(String message, Class<?> schema) {
        // build request
        McpSchema.ElicitRequest elicitRequest = buildElicitationRequest(message, schema);
        // 4.0 进行请求
        // 4.0 request
        return exchange.createElicitation(elicitRequest);
    }
    /**
     * 通过 exchange 向 客户端 发起elicitation 请求, 指定反序列化的 格式, 异步的话
     *
     * @param message 请求的消息
     * @param schema  反序列化的格式
     * @return McpSchema.ElicitResult
     */
    @Override
    public Mono<McpSchema.ElicitResult> elicitAsync(String message, Class<?> schema) {

        // return as Mono
        return Mono.fromCallable(() -> this.elicit(message, schema))
                .subscribeOn(Schedulers.boundedElastic());
    }



}
