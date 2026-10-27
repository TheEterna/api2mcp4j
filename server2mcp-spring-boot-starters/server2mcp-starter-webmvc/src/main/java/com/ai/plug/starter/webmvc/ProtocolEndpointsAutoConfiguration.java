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
 * @module: server2mcp-starter-webmvc
 * @layer: starter
 * @since: 2026/8/3
 * @author: han
 * @updated: 2026-10-27 14:42
 *
 * Spring Boot auto-configuration that mounts the framework's protocol-2026-07-28
 * HTTP contracts (Discover, Tasks, Notifications, AugmentedPrompts, JSON-RPC, SSE)
 * at {@code /mcp/...} endpoints.
 *
 * <p>This bridges the gap between Java SDK 2.0 (no JSON-RPC schema for the
 * new RPC routes) and the MCP 2026-07-28 protocol. When Java SDK ≥ 3.0.0
 * ships native routes, the controllers remain as a fallback for older
 * clients — no business-code change required.
 *
 * <h2>与 Web 栈无关的协议 bean 归属</h2>
 *
 * <p>任务存储 / 增强提示存储 / 通知记录器 / 能力快照 / JSON-RPC 路由表等
 * <b>协议层</b> bean 已下沉到
 * {@code com.ai.plug.autoconfigure.ProtocolCoreAutoConfiguration}，由 Servlet 与
 * Reactive 两个 starter 共用同一份注册定义，避免同一份协议契约在两处各建一套
 * 而出现「注册面与暴露面脱钩」。
 *
 * <p>本类只负责 <b>Web 栈特有</b>的装配：Controller 挂载与 SSE 长连接形态。
 */

package com.ai.plug.starter.webmvc;

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
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configuration for the MCP protocol HTTP endpoints (Servlet stack).
 *
 * <p>Wires the framework-layer endpoints into Spring's application context as
 * singletons, then exposes them through dedicated MVC controllers under
 * {@code /mcp/...}.
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
 *
 * <p>All backing stores are exposed as Spring beans by
 * {@link ProtocolCoreAutoConfiguration}; user code can replace them with
 * Redis / JDBC implementations by registering a {@code @Primary} bean of the
 * corresponding type.
 */
@AutoConfiguration(after = ProtocolCoreAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ProtocolEndpointsAutoConfiguration {

    /** MVC configurer marker so Spring Boot picks up our controllers. */
    @Bean
    @ConditionalOnMissingBean
    public WebMvcConfigurer mcpProtocolEndpointsConfigurer() {
        return new WebMvcConfigurer() { };
    }

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
     * protocol-2026-07-28 {@code subscriptions/listen}. Bridges the
     * polling endpoint's recordEvent to every live SSE client via a
     * listener hook.
     */
    @Bean
    @ConditionalOnMissingBean(SseNotificationsController.class)
    public SseNotificationsController sseNotificationsController(
            NotificationsPollingEndpoint notifications) {
        SseNotificationsController controller = new SseNotificationsController(notifications);
        notifications.setListener(controller::broadcast);
        controller.startHeartbeat();
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
