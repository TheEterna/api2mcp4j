package com.ai.plug.core.spec.utils.progress;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

/**
 * @header-start
 * @module McpAsyncProgress
 * @keywords 进度上报 notifications/progress progressToken 异步实现 McpProgress Reactive Mono
 *           no-op 无token 进度通知 协议2025-11-25 进度百分比 链式订阅
 * @updated 2026-10-28 12:20
 * @purpose 异步版进度上报器，无 progressToken 时退化为 no-op
 * @layer core
 * @see McpProgress — 进度抽象接口
 * @see McpProgressFactory — 进度实例工厂
 * @author han
 * @header-end
 */

/**
 * 异步版 {@link McpProgress}，通过 {@code McpAsyncServerExchange.progressNotification} 发
 * （返回 {@link Mono}）。{@link #report(double)} 内部 subscribe；上游链式工具方法通常不
 * 直接调本接口，而是经 callback 处理 subscribe。
 *
 * @author han
 * @time 2026/7/31 17:50
 */
public class McpAsyncProgress implements McpProgress {

    private final McpAsyncServerExchange exchange;

    @Nullable
    private final Object progressToken;

    private final boolean noOp;

    public McpAsyncProgress(McpAsyncServerExchange exchange, @Nullable Object progressToken) {
        this.exchange = exchange;
        this.progressToken = progressToken;
        this.noOp = progressToken == null;
    }

    @Override
    public void report(double progress) {
        if (noOp) {
            return;
        }
        exchange.progressNotification(McpSchema.ProgressNotification.builder(progressToken, progress).build())
                .subscribe();
    }

    @Override
    public void report(double progress, String message) {
        if (noOp) {
            return;
        }
        exchange.progressNotification(
                McpSchema.ProgressNotification.builder(progressToken, progress).message(message).build())
                .subscribe();
    }

    @Override
    public boolean isNoOp() {
        return noOp;
    }

    @Override
    @Nullable
    public String progressToken() {
        return progressToken == null ? null : progressToken.toString();
    }

    /**
     * 异步订阅入口：返回 {@link Mono} 而非 fire-and-forget 的 report，便于上游链式组合。
     */
    public Mono<Void> progressNotification(double progress, @Nullable String message) {
        if (noOp) {
            return Mono.empty();
        }
        return exchange.progressNotification(
                McpSchema.ProgressNotification.builder(progressToken, progress).message(message).build());
    }
}