/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ai.plug.starter.webflux;

import com.ai.plug.core.spec.change.NotificationsPollingEndpoint;
import com.ai.plug.core.spec.discover.DiscoverEndpoint;
import com.ai.plug.core.spec.jsonrpc.JsonRpcRouter;
import com.ai.plug.core.spec.tasks.AugmentedPromptEndpoint;
import com.ai.plug.core.spec.tasks.TasksEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.test.web.reactive.server.WebTestClient.bindToController;

/**
 * Integration test verifying that the framework's protocol-2026-07-28
 * HTTP endpoints actually mount on the <b>Reactive</b> stack — i.e. the
 * HTTP-layer substitutes for SDK-2.0-missing RPC routes are reachable
 * via real HTTP, not just unit-tested contracts.
 *
 * <p>Mirrors {@code com.ai.plug.starter.webmvc.ProtocolEndpointsIntegrationTest}
 * in both assertions and URI set — the two stacks must stay wire-compatible.
 *
 * <p>Uses standalone {@code WebTestClient.bindToController} setup to avoid
 * pulling in the test autoconfigure artifacts Spring Boot 4.x split out.
 */
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = {
    "spring.main.web-application-type=reactive"
})
class ProtocolEndpointsIntegrationTest {

    @Autowired private DiscoverEndpoint discoverEndpoint;
    @Autowired private TasksEndpoint tasksEndpoint;
    @Autowired private AugmentedPromptEndpoint augmentedPromptEndpoint;
    @Autowired private NotificationsPollingEndpoint notificationsEndpoint;
    @Autowired private JsonRpcRouter jsonRpcRouter;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = bindToController(
            new DiscoverController(discoverEndpoint),
            new TasksController(tasksEndpoint),
            new NotificationsController(notificationsEndpoint),
            new AugmentedPromptsController(augmentedPromptEndpoint),
            new JsonRpcController(jsonRpcRouter)
        ).build();
    }

    @Test
    void discover_endpointRespondsWithProtocolShape() {
        client.get().uri("/mcp/discover")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.protocolVersions").isArray()
            .jsonPath("$.preferredVersion").isEqualTo("2026-07-28")
            .jsonPath("$.serverInfo.name").exists()
            .jsonPath("$.capabilities").exists();
    }

    @Test
    void tasks_listEndpointReturnsArray() {
        client.get().uri("/mcp/tasks")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.tasks").isArray();
    }

    @Test
    void notifications_pollEndpointReturnsEnvelope() {
        client.get().uri("/mcp/notifications?since=-1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.events").isArray();
    }

    @Test
    void jsonRpc_dispatchReturnsProtocolEnvelope() {
        client.post().uri("/mcp/jsonrpc")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"server/discover\",\"params\":{}}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.jsonrpc").isEqualTo("2.0")
            .jsonPath("$.result.serverInfo.name").exists();
    }

    @Test
    void jsonRpc_unknownMethodReturnsJsonRpcError() {
        client.post().uri("/mcp/jsonrpc")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"no/such/method\",\"params\":{}}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.error.code").exists();
    }
}
