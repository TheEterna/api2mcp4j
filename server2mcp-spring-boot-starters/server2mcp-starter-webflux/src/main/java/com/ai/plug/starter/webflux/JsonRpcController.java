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

import com.ai.plug.core.spec.jsonrpc.JsonRpcResponse;
import com.ai.plug.core.spec.jsonrpc.JsonRpcRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Spring WebFlux controller that exposes the protocol-2026-07-28 JSON-RPC
 * endpoint at {@code POST /mcp/jsonrpc}. Accepts a single JSON-RPC
 * envelope and returns a {@link JsonRpcResponse}.
 *
 * <p>This is the reactive twin of
 * {@code com.ai.plug.starter.webmvc.JsonRpcController} — same URI, same wire
 * shape, same router bean; only the return type differs ({@code Mono} vs
 * a plain value).
 *
 * <p>Dispatch runs inside {@link Mono#fromSupplier} so it happens on
 * subscription rather than at invocation time — no blocking call on the
 * event loop, and no {@code .block()} anywhere in the reactive chain.
 *
 * <p>Once Java MCP SDK ≥ 3.0.0 ships its native router, this controller
 * becomes a fallback (zero behavior change — the wire shape is identical).
 *
 * @author han
 * @time 2026/10/27
 */
@RestController
public class JsonRpcController {

    private static final Logger logger = LoggerFactory.getLogger(JsonRpcController.class);

    private final JsonRpcRouter router;

    public JsonRpcController(JsonRpcRouter router) {
        if (router == null) {
            throw new IllegalArgumentException("router is required");
        }
        this.router = router;
    }

    @PostMapping(value = "/mcp/jsonrpc",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<JsonRpcResponse> dispatch(@RequestBody Map<String, Object> body) {
        logger.debug("JSON-RPC dispatch: method={}", body.get("method"));
        return Mono.fromSupplier(() -> router.dispatchRaw(body))
                .map(response -> response == null
                        ? JsonRpcResponse.error(
                            JsonRpcResponse.JsonRpcError.of(
                                JsonRpcResponse.JsonRpcError.INVALID_REQUEST,
                                "Body must be a JSON-RPC request envelope"),
                            null)
                        : response);
    }
}
