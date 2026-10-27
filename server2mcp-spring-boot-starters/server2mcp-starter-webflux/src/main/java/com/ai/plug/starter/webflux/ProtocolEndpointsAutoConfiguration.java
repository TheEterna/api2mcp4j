/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @file: ProtocolEndpointsAutoConfiguration.java
 * @module: server2mcp-starter-webflux
 * @layer: starter
 * @since: 2026/10/27
 * @author: han
 * @updated: 2026-10-27 14:45
 *
 * Spring Boot auto-configuration that mounts the framework's protocol-2026-07-28
 * HTTP contracts (Discover, Tasks, Notifications, AugmentedPrompts, JSON-RPC, SSE)
 * at {@code /mcp/...} endpoints on the <b>Reactive</b> stack.
 *
 * <p>与 {@code server2mcp-starter-webmvc} 的 {@code ProtocolEndpointsAutoConfiguration}
 * 是<b>对称</b>关系：暴露的 URI 与 wire shape 完全一致，差别只在返回类型是
 * {@code Mono} / {@code Flux} 而非同步值。
 *
 * <p>协议层 bean（任务存储 / 通知记录器 / JSON-RPC 路由表等）不在本类定义 ——
 * 它们由 {@code com.ai.plug.autoconfigure.ProtocolCoreAutoConfiguration} 统一提供，
 * 两个 Web 栈共用同一份注册定义。
 */

package com.ai.plug.starter.webflux;

import com.ai.plug.autoconfigure.ProtocolCoreAutoConfiguration;
import com.ai.plug.core.spec.change.NotificationsPollingEndpoint;
import com.ai.plug.core.spec.discover.DiscoverEndpoint;
import com.ai.plug.core.spec.jsonrpc.JsonRpcRouter;
import com.ai.plug.core.spec.tasks.AugmentedPromptEndpoint;
import com.ai.plug.core.spec.tasks.TasksEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the MCP protocol HTTP endpoints (Reactive stack).
 *
 * <h2>Endpoints exposed</h2>
 * <ul>
 *   <li>{@code GET /mcp/discover} → {@link DiscoverController}</li>
 *   <li>{@code GET /mcp/tasks} / {@code /mcp/tasks/{id}} / {@code POST /mcp/tasks/{id}/cancel}
 *       → {@link TasksController}</li>
 *   <li>{@code GET /mcp/tasks/{id}/augmented-prompts} / {@code POST /mcp/tasks/{id}/augmented-prompts/drain}
 *       → {@link AugmentedPromptsController}</li>
 *   <li>{@code GET /mcp/notifications} → {@link NotificationsController}</li>
 *   <li>{@code POST /mcp/jsonrpc} → {@link JsonRpcController}</li>
 *   <li>{@code GET /mcp/sse} → {@link SseNotificationsController}</li>
 * </ul>
 */
@AutoConfiguration(after = ProtocolCoreAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ProtocolEndpointsAutoConfiguration {

    /**
     * JSON-RPC controller — exposes {@code POST /mcp/jsonrpc} on top of
     * the {@link JsonRpcRouter} bean.
     */
    @Bean
    @ConditionalOnMissingBean(JsonRpcController.class)
    public JsonRpcController jsonRpcController(JsonRpcRouter router) {
        return new JsonRpcController(router);
    }

    /**
     * SSE notifications controller — exposes {@code GET /mcp/sse} for
     * protocol-2026-07-28 {@code subscriptions/listen}. Bridges the polling
     * endpoint's recordEvent to every live SSE client via a listener hook.
     *
     * <p>与 Servlet 版不同的是这里<b>不需要</b>启动心跳线程 —— Reactive 版的心跳由
     * {@code Flux.interval} 提供，订阅即生效，随订阅结束自动停止。
     */
    @Bean
    @ConditionalOnMissingBean(SseNotificationsController.class)
    public SseNotificationsController sseNotificationsController(
            NotificationsPollingEndpoint notifications) {
        SseNotificationsController controller = new SseNotificationsController(notifications);
        notifications.setListener(controller::broadcast);
        return controller;
    }

    // ---- HTTP legacy endpoints (kept as fallback for older clients) ----

    @Bean
    @ConditionalOnMissingBean(DiscoverController.class)
    public DiscoverController discoverController(DiscoverEndpoint discoverEndpoint) {
        return new DiscoverController(discoverEndpoint);
    }

    @Bean
    @ConditionalOnMissingBean(TasksController.class)
    public TasksController tasksController(TasksEndpoint tasksEndpoint) {
        return new TasksController(tasksEndpoint);
    }

    @Bean
    @ConditionalOnMissingBean(NotificationsController.class)
    public NotificationsController notificationsController(NotificationsPollingEndpoint notifications) {
        return new NotificationsController(notifications);
    }

    @Bean
    @ConditionalOnMissingBean(AugmentedPromptsController.class)
    public AugmentedPromptsController augmentedPromptsController(AugmentedPromptEndpoint endpoint) {
        return new AugmentedPromptsController(endpoint);
    }
}
