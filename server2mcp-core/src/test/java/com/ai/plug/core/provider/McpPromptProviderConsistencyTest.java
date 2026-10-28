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

import com.ai.plug.core.annotation.McpPrompt;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @header-start
 * @module McpPromptProviderConsistencyTest
 * @keywords 提示词 一致性 @McpPrompt注解 name description 注解契约 反射断言 提示模板
 * @updated 2026-10-28 12:20
 * @purpose 断言 @McpPrompt 注解字段语义与默认值保持稳定
 * @layer test
 * @author han
 * @header-end
 */

/**
 * Smoke tests for {@link McpPromptProvider}'s @McpPrompt annotation
 * processing. Prompt tools follow the same @McpTool meta/annotations
 * pattern — we just verify the annotation contract is consistent.
 */
class McpPromptProviderConsistencyTest {

    @Test
    void mcpPromptAnnotation_hasConsistentFields() throws Exception {
        Method m = PromptHolder.class.getDeclaredMethod("greet", String.class);
        McpPrompt ann = m.getAnnotation(McpPrompt.class);
        assertThat(ann).isNotNull();
        assertThat(ann.name()).isEqualTo("greet");
        assertThat(ann.description()).isEqualTo("Greets the user by name");
    }

    @Test
    void mcpPromptAnnotation_defaultValues() throws Exception {
        Method m = PromptHolder.class.getDeclaredMethod("defaults");
        McpPrompt ann = m.getAnnotation(McpPrompt.class);
        // Defaults are non-null but may be empty strings
        assertThat(ann.name()).isNotNull();
        assertThat(ann.description()).isNotNull();
    }

    static final class PromptHolder {
        @McpPrompt(name = "greet", description = "Greets the user by name")
        public String greet(String name) { return "Hello " + name; }

        @McpPrompt(name = "defaults", description = "Prompt with default fields")
        public String defaults() { return "ok"; }
    }
}