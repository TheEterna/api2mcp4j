/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ai.plug.core.spec.change;

import com.ai.plug.core.annotation.McpTool;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @header-start
 * @module McpToolChangeNotifierListChangedTest
 * @keywords 变更通知 listChanged 注解默认值 显式false跳过 工具清单变更 通知抑制
 * @updated 2026-10-28 12:20
 * @purpose 断言工具变更通知器的 listChanged 判定与通知抑制规则
 * @layer test
 * @author han
 * @header-end
 */

class McpToolChangeNotifierListChangedTest {

    @Test
    void isListChanged_nullAnnotationDefaultsTrue() {
        assertThat(McpToolChangeNotifier.isListChanged(null)).isTrue();
    }

    @Test
    void isListChanged_defaultTrue() throws Exception {
        McpTool ann = Sample.class.getDeclaredMethod("defaultTrue").getAnnotation(McpTool.class);
        assertThat(McpToolChangeNotifier.isListChanged(ann)).isTrue();
    }

    @Test
    void isListChanged_explicitTrue() throws Exception {
        McpTool ann = Sample.class.getDeclaredMethod("explicitTrue").getAnnotation(McpTool.class);
        assertThat(McpToolChangeNotifier.isListChanged(ann)).isTrue();
    }

    @Test
    void isListChanged_explicitFalseSkipsNotification() throws Exception {
        McpTool ann = Sample.class.getDeclaredMethod("explicitFalse").getAnnotation(McpTool.class);
        assertThat(McpToolChangeNotifier.isListChanged(ann)).isFalse();
    }

    static final class Sample {
        @McpTool(name = "a")
        public String defaultTrue() { return "x"; }

        @McpTool(name = "b", listChanged = true)
        public String explicitTrue() { return "x"; }

        @McpTool(name = "c", listChanged = false)
        public String explicitFalse() { return "x"; }
    }
}