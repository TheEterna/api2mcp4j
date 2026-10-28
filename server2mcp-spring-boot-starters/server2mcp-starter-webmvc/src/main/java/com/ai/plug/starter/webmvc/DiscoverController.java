/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ai.plug.starter.webmvc;

import com.ai.plug.core.spec.discover.DiscoverEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @header-start
 * @module DiscoverController
 * @keywords 发现 server/discover 协议2026-07-28 SpringMVC控制器 DiscoverEndpoint
 *           协议版本协商 HTTP替身 Servlet栈
 * @updated 2026-10-28 12:20
 * @purpose 在 Servlet 栈暴露服务发现端点，替身 SDK 缺失的 server/discover RPC
 * @layer starter
 * @see DiscoverEndpoint — 端点背后的协议层实现
 * @author han
 * @header-end
 */

/**
 * Spring MVC controller exposing {@code GET /mcp/discover} — the
 * framework-layer HTTP substitute for the protocol-2026-07-28
 * {@code server/discover} JSON-RPC method (which Java SDK 2.0 lacks).
 *
 * <p>Mounted automatically by
 * {@link ProtocolEndpointsAutoConfiguration}.
 */
@RestController
@RequestMapping("/mcp")
public class DiscoverController {

    private final DiscoverEndpoint endpoint;

    public DiscoverController(DiscoverEndpoint endpoint) {
        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint is required");
        }
        this.endpoint = endpoint;
    }

    @GetMapping("/discover")
    public Map<String, Object> discover() {
        return endpoint.handle();
    }
}