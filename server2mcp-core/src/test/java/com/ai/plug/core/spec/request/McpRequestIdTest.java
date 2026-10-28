/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ai.plug.core.spec.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @header-start
 * @module McpRequestIdTest
 * @keywords 请求ID of空值单例 合成ID 前缀 req 存在值 关联标识 值对象
 * @updated 2026-10-28 12:20
 * @purpose 断言请求 ID 值对象的空值单例、合成与前缀语义
 * @layer test
 * @author han
 * @header-end
 */

class McpRequestIdTest {

    @Test
    void of_nullIsNoneSingleton() {
        assertThat(McpRequestId.of(null)).isSameAs(McpRequestId.NONE);
        assertThat(McpRequestId.NONE.isPresent()).isFalse();
    }

    @Test
    void of_withValueIsPresent() {
        McpRequestId id = McpRequestId.of("abc-123");
        assertThat(id.id()).isEqualTo("abc-123");
        assertThat(id.isPresent()).isTrue();
    }

    @Test
    void synthetic_producesUniquePrefixedId() {
        McpRequestId a = McpRequestId.synthetic("call");
        McpRequestId b = McpRequestId.synthetic("call");
        assertThat(a.id()).startsWith("call-");
        assertThat(b.id()).startsWith("call-");
        assertThat(a.id()).isNotEqualTo(b.id());
    }

    @Test
    void synthetic_nullPrefixDefaultsToReq() {
        McpRequestId id = McpRequestId.synthetic(null);
        assertThat(id.id()).startsWith("req-");
    }
}