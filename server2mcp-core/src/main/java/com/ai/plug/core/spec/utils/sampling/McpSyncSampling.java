package com.ai.plug.core.spec.utils.sampling;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @header-start
 * @module McpSyncSampling
 * @keywords 采样 sampling createMessage 同步实现 McpSampling LLMOptions
 *           McpSyncServerExchange LLM调用 反向采样 客户端模型
 * @updated 2026-10-28 12:20
 * @purpose 同步向客户端发起采样请求，反向调用客户端的 LLM 能力
 * @layer core
 * @see McpSampling — 采样抽象接口
 * @see McpSamplingFactory — 采样实例工厂
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/7/8 18:08
 */

public non-sealed class McpSyncSampling implements McpSampling {


    private final McpSyncServerExchange exchange;

    public McpSyncSampling(McpSyncServerExchange exchange) {
        this.exchange = exchange;
    }

    /**
     * 向客户端发送同步采样请求 Send synchronous sampling request to the client
     *
     * @param messages   消息列表 message list
     * @param models     待选模型列表(仅供客户端参考) List of models to be selected (for client reference only)
     * @param llmOptions 模型参数 model parameter
     * @return McpSchema.CreateMessageResult
     */
    @Override
    public McpSchema.CreateMessageResult sample(List<McpSchema.SamplingMessage> messages, List<String> models, LLMOptions llmOptions) {

        McpSchema.CreateMessageRequest sampleRequest = buildSamplingRequest(messages, models, llmOptions);
        return exchange.createMessage(sampleRequest);
    }

    /**
     * 向客户端发送异步采样请求 Send asynchronous sampling request to the client
     *
     * @param messages   消息列表 message list
     * @param models     待选模型列表(仅供客户端参考) List of models to be selected (for client reference only)
     * @param llmOptions 模型参数 model parameter
     * @return McpSchema.CreateMessageResult
     */
    @Override
    public Mono<McpSchema.CreateMessageResult> sampleAsync(List<McpSchema.SamplingMessage> messages, List<String> models, LLMOptions llmOptions) {

        // 以Mono形式返回
        // Return as mono
        return Mono.just(this.sample(messages, models, llmOptions));
    }
}
