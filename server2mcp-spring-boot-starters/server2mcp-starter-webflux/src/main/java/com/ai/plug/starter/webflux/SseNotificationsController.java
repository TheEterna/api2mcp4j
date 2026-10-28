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

package com.ai.plug.starter.webflux;

import com.ai.plug.core.spec.change.NotificationsPollingEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @header-start
 * @module SseNotificationsController
 * @keywords SSE Server-Sent-Events subscriptions/listen 协议2026-07-28 Flux Sinks多播
 *           Last-Event-ID 断线重放 心跳ping 通知推送 Reactive栈 无阻塞
 * @updated 2026-10-28 12:20
 * @purpose 在响应式栈以 SSE 长连接推送通知事件，支持断线重放与心跳保活
 * @layer starter
 * @see NotificationsPollingEndpoint — 事件来源与重放缓冲
 * @author han
 * @header-end
 */

/**
 * Server-Sent Events long-poll endpoint for protocol-2026-07-28
 * {@code subscriptions/listen} on the <b>Reactive</b> stack. The wire shape:
 * clients open {@code GET /mcp/sse} and receive a stream of
 * {@code data: <json>} lines, one per notification event the framework
 * records via {@link NotificationsPollingEndpoint#recordEvent}.
 *
 * <p>This is the reactive twin of
 * {@code com.ai.plug.starter.webmvc.SseNotificationsController} — same URI,
 * same wire shape, same replay semantics; the difference is the transport
 * substrate (Reactor {@link Sinks} + {@link Flux} instead of
 * {@code SseEmitter} + a scheduled heartbeat thread).
 *
 * <h2>Resume via {@code Last-Event-ID}</h2>
 *
 * <p>If the client supplies a {@code Last-Event-ID} header (per the SSE
 * spec), this controller replays every event whose id is greater than that
 * value before going live. This makes reconnects lossless up to the ring
 * buffer's capacity.
 *
 * <h2>Heartbeat</h2>
 *
 * <p>An SSE comment line ({@code ": ping"}) is sent every 15 seconds so
 * intermediaries (proxies, browsers) don't close the connection on idle
 * timeout. On this stack the heartbeat is a {@link Flux#interval} — no
 * dedicated scheduler thread, and no blocking call anywhere.
 *
 * @author han
 * @time 2026/10/27
 */
@RestController
public class SseNotificationsController {

    private static final Logger logger = LoggerFactory.getLogger(SseNotificationsController.class);

    /** Heartbeat interval — matches the Servlet twin so both stacks behave alike. */
    private static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(15);

    private final NotificationsPollingEndpoint notifications;

    /**
     * Multicast sink: one upstream, many subscribers. Events emitted while no
     * client is connected are dropped here — but the framework's own ring
     * buffer ({@link NotificationsPollingEndpoint}) still holds them, so a
     * reconnecting client can replay via {@code Last-Event-ID}.
     */
    private final Sinks.Many<ServerSentEvent<?>> sink =
            Sinks.many().multicast().onBackpressureBuffer();

    public SseNotificationsController(NotificationsPollingEndpoint notifications) {
        if (notifications == null) {
            throw new IllegalArgumentException("notifications is required");
        }
        this.notifications = notifications;
    }

    @GetMapping(value = "/mcp/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<?>> subscribe(
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        long since = parseCursor(lastEventId);
        logger.debug("SSE client subscribed: replaySince={}", since);

        // Replay missed events first, then go live.
        return Flux.concat(replayEvents(since), Flux.merge(liveEvents(), heartbeat()));
    }

    /**
     * Push a freshly recorded event to all live SSE clients. Called by
     * {@link NotificationsPollingEndpoint#recordEvent} via the listener hook
     * wired in {@link ProtocolEndpointsAutoConfiguration}.
     *
     * <p>{@code tryEmitNext} never throws: when nobody is subscribed the
     * sink reports {@code FAIL_ZERO_SUBSCRIBER} and the event is simply
     * dropped from the live channel (it stays in the framework's ring buffer
     * for later replay).
     */
    public void broadcast(long cursor, String kind, Map<String, Object> payload) {
        sink.tryEmitNext(ServerSentEvent.builder()
                .id(String.valueOf(cursor))
                .event(kind)
                .data(payload)
                .build());
    }

    /**
     * Replay the events the client missed, taken from the framework's ring
     * buffer via {@link NotificationsPollingEndpoint#handlePoll}.
     */
    private Flux<ServerSentEvent<?>> replayEvents(long since) {
        if (since < 0) {
            return Flux.empty();
        }
        Map<String, Object> replay = notifications.handlePoll(since);
        Object eventsObj = replay.get("events");
        if (!(eventsObj instanceof List<?> list)) {
            return Flux.empty();
        }
        return Flux.fromIterable(list)
                .filter(NotificationsPollingEndpoint.NotificationEvent.class::isInstance)
                .map(NotificationsPollingEndpoint.NotificationEvent.class::cast)
                .map(ne -> ServerSentEvent.builder()
                        .id(String.valueOf(ne.cursor()))
                        .event(ne.kind())
                        .data(ne.payload())
                        .build());
    }

    /** The live channel — events pushed by {@link #broadcast}. */
    private Flux<ServerSentEvent<?>> liveEvents() {
        return sink.asFlux();
    }

    /** Periodic SSE comment line keeping intermediaries from closing the connection. */
    private Flux<ServerSentEvent<?>> heartbeat() {
        return Flux.interval(HEARTBEAT_INTERVAL)
                .map(tick -> ServerSentEvent.builder().comment("ping").build());
    }

    private static long parseCursor(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
