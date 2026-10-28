/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ai.plug.core.spec.resulttype;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @header-start
 * @module WireSchemaBenchmarkTest
 * @keywords wire基准 校验器基准 性能基准 报告字段 toString 校验开销
 * @updated 2026-10-28 12:20
 * @purpose 断言 wire 校验基准测试的输出字段与结果形态
 * @layer test
 * @author han
 * @header-end
 */

class WireSchemaBenchmarkTest {

    @Test
    void run_returnsResultWithExpectedFields() throws Exception {
        var r = WireSchemaBenchmark.run(100);
        assertThat(r.iterations()).isEqualTo(100);
        assertThat(r.meanNs()).isPositive();
        assertThat(r.opsPerSec()).isPositive();
        assertThat(r.totalNs()).isPositive();
    }

    @Test
    void runFull_returnsValidResult() throws Exception {
        var r = WireSchemaBenchmark.runFull(50);
        assertThat(r.iterations()).isEqualTo(50);
        assertThat(r.meanNs()).isPositive();
    }

    @Test
    void resultToString_includesAllFields() throws Exception {
        var r = WireSchemaBenchmark.run(10);
        String s = r.toString();
        assertThat(s)
            .contains("iters=10")
            .contains("ns/op")
            .contains("ops/s");
    }
}