package com.ai.plug.core;

import com.ai.plug.core.spec.utils.logging.McpLogger;
import org.springframework.util.ClassUtils;

/**
 * @header-start
 * @module TestClass
 * @keywords 临时试验台 main入口 ClassUtils isAssignable McpLogger 类型判定 调试脚手架
 * @updated 2026-10-28 12:20
 * @purpose 一次性 main 脚手架，验证 Object 与 McpLogger 的可赋值关系
 * @layer test
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/7/3 1:15
 */

public class TestClass {
    public static void main(String[] args) {
        System.out.println(ClassUtils.isAssignable(Object.class, McpLogger.class));
    }
}
