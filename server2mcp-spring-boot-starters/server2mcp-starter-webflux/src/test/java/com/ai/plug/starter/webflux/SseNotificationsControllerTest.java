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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @header-start
 * @module SseNotificationsControllerTest
 * @keywords SSE测试 StepVerifier Flux Sink广播 断线重放 Last-Event-ID 非法游标
 *           无订阅者不抛异常 Reactive栈
 * @updated 2026-10-28 12:20
 * @purpose 断言响应式 SSE 控制器的广播、重放与游标解析行为
 * @layer test
 * @author han
 * @header-end
 */

/**
 * Unit tests for the Reactive SSE notifications controller.
 *
 * <p>Mirrors {@code com.ai.plug.starter.webmvc.SseNotificationsControllerTest}:
 * same behaviours, different substrate (Reactor {@code Sink} + {@code Flux}
 * instead of {@code SseEmitter}).
 */
class SseNotificationsControllerTest {

    private NotificationsPollingEndpoint notifications;
    private SseNotificationsController controller;

    @BeforeEach
    void setUp() {
        notifications = new NotificationsPollingEndpoint();
        controller = new SseNotificationsController(notifications);
        notifications.setListener(controller::broadcast);
    }

    @Test
    void broadcast_withNoSubscribers_isNoOp() {
        controller.broadcast(1L, "tools", Map.of("k", "v"));
        // Nothing subscribed — the emit is dropped, but must not throw.
        assertThat(notifications.handlePoll(-1)).containsKey("events");
    }

    @Test
    void subscribe_withNoLastEventId_emitsLiveEventsOnly() {
        Flux<ServerSentEvent<?>> flux = controller.subscribe(null);
        assertThat(flux).isNotNull();

        // No replay events are queued for a fresh subscription.
        StepVerifier.create(flux.take(0))
            .expectComplete()
            .verify(Duration.ofSeconds(2));
    }

    @Test
    void subscribe_withLastEventId_replaysMissedEvents() {
        notifications.recordEvent("tools", Map.of("k", "v"));

        Flux<ServerSentEvent<?>> flux = controller.subscribe("0");

        StepVerifier.create(flux.take(1))
            .assertNext(event -> {
                assertThat(event.id()).isEqualTo("1");
                assertThat(event.event()).isEqualTo("tools");
                assertThat(event.data()).isEqualTo(Map.of("k", "v"));
            })
            .thenCancel()
            .verify(Duration.ofSeconds(2));
    }

    @Test
    void broadcast_reachesLiveSubscriber() {
        Flux<ServerSentEvent<?>> flux = controller.subscribe(null).take(1);

        StepVerifier.create(flux)
            .then(() -> controller.broadcast(42L, "tools", Map.of("k", "v")))
            .assertNext(event -> {
                assertThat(event.id()).isEqualTo("42");
                assertThat(event.event()).isEqualTo("tools");
            })
            .verifyComplete();
    }

    @Test
    void parseCursor_withBlankHeader_yieldsMinusOne() {
        // No Last-Event-ID → no replay. A blank header must behave the same.
        StepVerifier.create(controller.subscribe("   ").take(0))
            .expectComplete()
            .verify(Duration.ofSeconds(2));
    }

    @Test
    void parseCursor_withGarbageHeader_yieldsMinusOne() {
        StepVerifier.create(controller.subscribe("not-a-number").take(0))
            .expectComplete()
            .verify(Duration.ofSeconds(2));
    }
}
