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

import com.ai.plug.core.spec.tasks.AugmentedPromptEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @header-start
 * @module AugmentedPromptsController
 * @keywords 增强提示 tasks/augmented-prompt 协议2026-07-28 WebFlux控制器 Mono响应式
 *           AugmentedPromptEndpoint 列举 drain排空 JSON-RPC替身 Reactive栈
 * @updated 2026-10-28 12:20
 * @purpose 在响应式栈暴露增强提示的列举与排空端点，替身 SDK 缺失的 RPC 方法
 * @layer starter
 * @see AugmentedPromptEndpoint — 端点背后的协议层实现
 * @author han
 * @header-end
 */

/**
 * Spring WebFlux controller exposing {@code /mcp/tasks/{id}/augmented-prompts}
 * endpoints — substitutes for protocol-2026-07-28
 * {@code tasks/augmented-prompt} JSON-RPC method (Java SDK 2.0 lacks schema).
 *
 * <p>Reactive twin of
 * {@code com.ai.plug.starter.webmvc.AugmentedPromptsController}.
 */
@RestController
@RequestMapping("/mcp/tasks/{id}/augmented-prompts")
public class AugmentedPromptsController {

    private final AugmentedPromptEndpoint endpoint;

    public AugmentedPromptsController(AugmentedPromptEndpoint endpoint) {
        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint is required");
        }
        this.endpoint = endpoint;
    }

    @GetMapping
    public Mono<Map<String, Object>> list(@PathVariable("id") String id) {
        return Mono.fromSupplier(() -> endpoint.handleList(id));
    }

    @PostMapping("/drain")
    public Mono<Map<String, Object>> drain(@PathVariable("id") String id) {
        return Mono.fromSupplier(() -> endpoint.handleDrain(id));
    }
}
