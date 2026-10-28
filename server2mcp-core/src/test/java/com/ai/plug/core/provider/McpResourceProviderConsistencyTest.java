/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ai.plug.core.provider;

import com.ai.plug.core.annotation.McpResource;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @header-start
 * @module McpResourceProviderConsistencyTest
 * @keywords 资源 一致性 @McpResource注解 mimeType uri 注解契约 反射断言 资源模板
 * @updated 2026-10-28 12:20
 * @purpose 断言 @McpResource 注解字段语义与默认 mimeType 保持稳定
 * @layer test
 * @author han
 * @header-end
 */

/**
 * Smoke tests for {@link McpResourceProvider}'s @McpResource annotation
 * processing. Resource tools follow the same @McpTool meta/icon/annotations
 * pattern as the tool provider — we just verify the annotation contract is
 * consistent (same field set, same defaults).
 */
class McpResourceProviderConsistencyTest {

    @Test
    void mcpResourceAnnotation_hasConsistentFields() throws Exception {
        Method m = ResourceHolder.class.getDeclaredMethod("read", String.class);
        McpResource ann = m.getAnnotation(McpResource.class);
        assertThat(ann).isNotNull();
        assertThat(ann.name()).isEqualTo("read");
        assertThat(ann.title()).isEqualTo("Read File");
        assertThat(ann.description()).isEqualTo("Reads a file from disk");
        assertThat(ann.uri()).isEqualTo("file:///tmp/{path}");
        assertThat(ann.mimeType()).isEqualTo("text/plain");
    }

    @Test
    void mcpResourceAnnotation_defaultMineType() throws Exception {
        Method m = ResourceHolder.class.getDeclaredMethod("defaults", String.class);
        McpResource ann = m.getAnnotation(McpResource.class);
        // Default mimeType comes from the @McpResource annotation default
        assertThat(ann.mimeType()).isNotBlank();
    }

    static final class ResourceHolder {
        @McpResource(name = "read", title = "Read File",
                description = "Reads a file from disk",
                uri = "file:///tmp/{path}",
                mimeType = "text/plain")
        public String read(String path) { return "content of " + path; }

        @McpResource(name = "defaults")
        public String defaults(String path) { return "x"; }
    }
}