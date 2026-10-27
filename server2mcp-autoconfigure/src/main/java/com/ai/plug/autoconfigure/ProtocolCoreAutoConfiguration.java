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
 * @file: ProtocolCoreAutoConfiguration.java
 * @module: server2mcp-autoconfigure
 * @layer: autoconfigure
 * @since: 2026/10/27
 * @author: han
 * @updated: 2026-10-27 14:40
 *
 * 协议 2026-07-28 共享契约的<b>单一装配源</b>。
 *
 * <p>本类只装配「与 Web 栈无关」的协议层对象：任务存储、增强提示存储、通知记录器、
 * 能力快照、JSON-RPC 路由表等。Servlet（Spring MVC）与 Reactive（Spring WebFlux）
 * 两个 starter 共用同一套 bean，避免同一份协议契约在两个 starter 里各建一套 ——
 * 那会造成「注册面与暴露面脱钩」：改了 core 的契约却只在一个 starter 生效。
 *
 * <p>Web 栈相关的部分（Controller 挂载、SSE 长连接形态）留在各自的 starter：
 * <ul>
 *   <li>{@code server2mcp-starter-webmvc} → {@code ProtocolEndpointsAutoConfiguration}
 *       （{@code @ConditionalOnWebApplication(type = SERVLET)}）</li>
 *   <li>{@code server2mcp-starter-webflux} → {@code ProtocolEndpointsAutoConfiguration}
 *       （{@code @ConditionalOnWebApplication(type = REACTIVE)}）</li>
 * </ul>
 *
 * <p>所有 bean 均带 {@code @ConditionalOnMissingBean}，用户可注册 {@code @Primary}
 * 实现替换内存存储（Redis / JDBC / etc.）而无需改动框架代码。
 *
 * <p>依赖方向保持 {@code common ← core ← autoconfigure ← starters} 不变：本模块
 * 只引 core，不引任何 Web 栈 starter —— 否则 Reactive 用户会被拖入 Servlet 栈。
 */

package com.ai.plug.autoconfigure;

import com.ai.plug.core.spec.capabilities.WireServerCapabilities;
import com.ai.plug.core.spec.change.NotificationsPollingEndpoint;
import com.ai.plug.core.spec.discover.DiscoverEndpoint;
import com.ai.plug.core.spec.jsonrpc.JsonRpcRouter;
import com.ai.plug.core.spec.jsonrpc.JsonRpcRoutes;
import com.ai.plug.core.spec.mrtr.InMemoryMrtrSessionStore;
import com.ai.plug.core.spec.mrtr.MrtrSessionStore;
import com.ai.plug.core.spec.tasks.AugmentedPromptEndpoint;
import com.ai.plug.core.spec.tasks.AugmentedPromptStore;
import com.ai.plug.core.spec.tasks.InMemoryAugmentedPromptStore;
import com.ai.plug.core.spec.tasks.InMemoryTaskStore;
import com.ai.plug.core.spec.tasks.TaskStore;
import com.ai.plug.core.spec.tasks.TasksEndpoint;
import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 协议层共享 bean 的自动配置。
 *
 * <p>Servlet / Reactive 两个 starter 的 {@code ProtocolEndpointsAutoConfiguration}
 * 均通过 {@code @AutoConfiguration(after = ProtocolCoreAutoConfiguration.class)}
 * 声明在本类之后，确保注入协议 bean 时它们已就绪。
 */
@AutoConfiguration
public class ProtocolCoreAutoConfiguration {

    /** 默认服务端名（用户未配置 {@code spring.ai.mcp.server.name} 时使用）。 */
    private static final String DEFAULT_SERVER_NAME = "server2mcp";

    /** 默认服务端版本（用户未配置 {@code spring.ai.mcp.server.version} 时使用）。 */
    private static final String DEFAULT_SERVER_VERSION = "1.0.0";

    /** 默认内存任务存储。可用 {@code @Primary @Bean} 覆盖为 Redis / JDBC 实现。 */
    @Bean
    @ConditionalOnMissingBean(TaskStore.class)
    public TaskStore inMemoryTaskStore() {
        return new InMemoryTaskStore();
    }

    /** 默认内存增强提示存储。可用 {@code @Primary @Bean} 覆盖。 */
    @Bean
    @ConditionalOnMissingBean(AugmentedPromptStore.class)
    public AugmentedPromptStore inMemoryAugmentedPromptStore() {
        return new InMemoryAugmentedPromptStore();
    }

    /** 通知记录器（可变更）。SSE / 轮询两个出口共享同一份事件流。 */
    @Bean
    @ConditionalOnMissingBean(NotificationsPollingEndpoint.class)
    public NotificationsPollingEndpoint notificationsPollingEndpoint() {
        return new NotificationsPollingEndpoint();
    }

    /** 任务端点，由 {@link TaskStore} 支撑。 */
    @Bean
    @ConditionalOnMissingBean(TasksEndpoint.class)
    public TasksEndpoint tasksEndpoint(TaskStore taskStore) {
        return new TasksEndpoint(taskStore);
    }

    /** 增强提示端点，由 {@link AugmentedPromptStore} 支撑。 */
    @Bean
    @ConditionalOnMissingBean(AugmentedPromptEndpoint.class)
    public AugmentedPromptEndpoint augmentedPromptEndpoint(AugmentedPromptStore store) {
        return new AugmentedPromptEndpoint(store);
    }

    /** 默认 MRTR 会话存储。可用 {@code @Primary @Bean} 覆盖。 */
    @Bean
    @ConditionalOnMissingBean(MrtrSessionStore.class)
    public MrtrSessionStore inMemoryMrtrSessionStore() {
        return new InMemoryMrtrSessionStore();
    }

    /**
     * 默认 2026-07-28 服务端能力快照（tools.subscription /
     * completions.listChanged / experimental.io.modelcontextprotocol/tasks）。
     * 可用 {@code @Primary @Bean} 覆盖。
     */
    @Bean
    @ConditionalOnMissingBean(WireServerCapabilities.class)
    public WireServerCapabilities wireServerCapabilities() {
        return WireServerCapabilities.full();
    }

    /**
     * {@code server/discover} 端点。
     *
     * <p>服务端名与版本取自 {@link McpServerProperties}（即
     * {@code spring.ai.mcp.server.name} / {@code .version}），与 MCP Server 自身声明的
     * {@code serverInfo} 保持一致 —— 协议语义上 {@code serverInfo.version} 描述的是
     * <b>这个 MCP 服务实例</b>的版本，而非框架版本。
     */
    @Bean
    @ConditionalOnMissingBean(DiscoverEndpoint.class)
    public DiscoverEndpoint discoverEndpoint(
            McpServerProperties serverProperties,
            WireServerCapabilities wireServerCapabilities) {
        Supplier<Object> capsSupplier = () -> wireServerCapabilities;
        Supplier<Map<String, Object>> extSupplier = Map::of;
        String name = (serverProperties != null && serverProperties.getName() != null
                && !serverProperties.getName().isBlank())
                ? serverProperties.getName() : DEFAULT_SERVER_NAME;
        String version = (serverProperties != null && serverProperties.getVersion() != null
                && !serverProperties.getVersion().isBlank())
                ? serverProperties.getVersion() : DEFAULT_SERVER_VERSION;
        return new DiscoverEndpoint(name, version, capsSupplier, extSupplier);
    }

    /**
     * JSON-RPC 路由表，预装协议 2026-07-28 的全部路由
     * （{@code server/discover}、{@code tasks/*}、{@code subscriptions/listen}、
     * {@code input_required/respond}）。由两个 starter 的 {@code JsonRpcController}
     * 挂在 {@code POST /mcp/jsonrpc} 上。
     */
    @Bean
    @ConditionalOnMissingBean(JsonRpcRouter.class)
    public JsonRpcRouter jsonRpcRouter(
            DiscoverEndpoint discoverEndpoint,
            TaskStore taskStore,
            TasksEndpoint tasksEndpoint,
            AugmentedPromptEndpoint augmentedPromptEndpoint,
            NotificationsPollingEndpoint notificationsEndpoint,
            MrtrSessionStore mrtrStore) {
        JsonRpcRouter router = new JsonRpcRouter();
        JsonRpcRoutes.registerAll(router,
                discoverEndpoint,
                taskStore,
                tasksEndpoint,
                augmentedPromptEndpoint,
                notificationsEndpoint,
                mrtrStore);
        return router;
    }
}
