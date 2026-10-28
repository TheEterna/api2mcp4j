package com.ai.plug.common.utils;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @header-start
 * @module ConvertUtil
 * @keywords 同步转异步 资源规范转换 resourceConvertAysnc AsyncResourceSpecification
 *           SyncResourceSpecification Mono boundedElastic Reactor 桥接适配
 * @updated 2026-10-28 12:20
 * @purpose 把同步资源规范转换成异步资源规范，使同步读处理器能在异步服务端复用
 * @layer common
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/6/17 11:04
 */

public class ConvertUtil {

    public static McpServerFeatures.AsyncResourceSpecification resourceConvertAysnc(McpServerFeatures.SyncResourceSpecification resource) {
        return resource == null ? null : new McpServerFeatures.AsyncResourceSpecification(resource.resource(), (exchange, req) -> {
            return Mono.fromCallable(() -> {
                return (McpSchema.ReadResourceResult)resource.readHandler().apply(new McpSyncServerExchange(exchange), req);
            }).subscribeOn(Schedulers.boundedElastic());
        });
    }

}
