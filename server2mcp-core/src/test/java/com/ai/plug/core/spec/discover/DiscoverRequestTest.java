/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"));
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ai.plug.core.spec.discover;

import com.ai.plug.core.spec.discover.DiscoverTypes.DiscoverRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @header-start
 * @module DiscoverRequestTest
 * @keywords 发现请求 协议版本字段 构造校验 空值拒绝 已知协议版本兼容
 * @updated 2026-10-28 12:20
 * @purpose 断言发现请求对象对协议版本字段的校验与兼容范围
 * @layer test
 * @author han
 * @header-end
 */

class DiscoverRequestTest {

    @Test
    void constructor_validProtocol() {
        DiscoverRequest req = DiscoverRequest.of("2026-07-28");
        assertThat(req.preferredProtocol()).isEqualTo("2026-07-28");
    }

    @Test
    void constructor_nullProtocolRejected() {
        assertThatThrownBy(() -> new DiscoverRequest(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("preferredProtocol is required");
    }

    @Test
    void constructor_blankProtocolRejected() {
        assertThatThrownBy(() -> new DiscoverRequest("   "))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void supportsAllKnownProtocolVersions() {
        // Placeholder for documenting the protocol version negotiation contract
        DiscoverRequest r1 = DiscoverRequest.of("2025-11-25");
        DiscoverRequest r2 = DiscoverRequest.of("2026-07-28");
        assertThat(r1.preferredProtocol()).isEqualTo("2025-11-25");
        assertThat(r2.preferredProtocol()).isEqualTo("2026-07-28");
    }
}