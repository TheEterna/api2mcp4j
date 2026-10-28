/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ai.plug.core.spec.inspection;

import com.ai.plug.core.context.tool.IToolContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @header-start
 * @module TasksInspectionHelperTest
 * @keywords 任务巡检 辅助工具 占位报告 空上下文不抛异常 工具上下文 巡检
 * @updated 2026-10-28 12:20
 * @purpose 断言任务巡检辅助方法在空上下文下的健壮行为
 * @layer test
 * @author han
 * @header-end
 */

class TasksInspectionHelperTest {

    @Test
    void inspect_returnsPlaceholderReport() {
        IToolContext ctx = proxyCtx();
        List<Map<String, Object>> report = TasksInspectionHelper.inspect(ctx);
        assertThat(report).hasSize(1);
        Map<String, Object> entry = report.get(0);
        assertThat(entry).containsKey("status");
        assertThat(entry.get("status").toString()).contains("not yet exposed");
    }

    @Test
    void inspect_nullContext_doesNotThrow() {
        // Stub should still return a sensible report even when context is null
        List<Map<String, Object>> report = TasksInspectionHelper.inspect(null);
        assertThat(report).hasSize(1);
    }

    private static IToolContext proxyCtx() {
        return (IToolContext) Proxy.newProxyInstance(
            TasksInspectionHelperTest.class.getClassLoader(),
            new Class<?>[]{IToolContext.class},
            (proxy, method, args) -> {
                if ("getRawTools".equals(method.getName())) {
                    return java.util.Map.of();
                }
                return null;
            });
    }
}