/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ai.plug.starter.webflux;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @header-start
 * @module TestApplication
 * @keywords 测试引导 SpringBootConfiguration 组件扫描 协议端点自动配置 Reactive栈
 *           最小测试上下文
 * @updated 2026-10-28 12:20
 * @purpose 最小测试引导类，通过组件扫描拉起响应式栈的协议端点自动配置
 * @layer test
 * @author han
 * @header-end
 */

/**
 * Minimal Spring Boot test configuration that pulls in the framework's
 * {@link ProtocolEndpointsAutoConfiguration} via component scan.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = ProtocolEndpointsAutoConfiguration.class)
public class TestApplication {
}
