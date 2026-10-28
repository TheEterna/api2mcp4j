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

import com.ai.plug.core.spec.tasks.TasksEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @header-start
 * @module TasksController
 * @keywords 任务端点 tasks/get tasks/list tasks/cancel 协议2026-07-28 WebFlux控制器 Mono
 *           TasksEndpoint 任务生命周期 JSON-RPC替身 Reactive栈
 * @updated 2026-10-28 12:20
 * @purpose 在响应式栈暴露任务的列举/查询/取消端点，替身 SDK 缺失的 tasks RPC
 * @layer starter
 * @see TasksEndpoint — 端点背后的协议层实现
 * @author han
 * @header-end
 */

/**
 * Spring WebFlux controller exposing {@code /mcp/tasks} endpoints —
 * substitutes for protocol-2026-07-28 {@code tasks/get}, {@code tasks/list},
 * {@code tasks/cancel} JSON-RPC methods (Java SDK 2.0 lacks schemas).
 *
 * <p>Reactive twin of {@code com.ai.plug.starter.webmvc.TasksController}.
 */
@RestController
@RequestMapping("/mcp/tasks")
public class TasksController {

    private final TasksEndpoint endpoint;

    public TasksController(TasksEndpoint endpoint) {
        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint is required");
        }
        this.endpoint = endpoint;
    }

    @GetMapping
    public Mono<Map<String, Object>> list() {
        return Mono.fromSupplier(endpoint::handleList);
    }

    @GetMapping("/{id}")
    public Mono<Map<String, Object>> get(@PathVariable("id") String id) {
        return Mono.fromSupplier(() -> endpoint.handleGet(id));
    }

    @PostMapping("/{id}/cancel")
    public Mono<Map<String, Object>> cancel(@PathVariable("id") String id,
                                            @RequestParam(value = "reason", required = false) String reason) {
        return Mono.fromSupplier(() -> endpoint.handleCancel(id, reason));
    }
}
