/*
 * Copyright 2025-2025 the original author or authors.
 */

package com.ai.plug.core.annotation;

import java.lang.annotation.*;

/** @header-start @module McpArg @keywords 参数注解 工具参数标记 MCP参数 exchange注入 上下文Map 参数名 参数描述 required 必填 @updated 2026-10-28 12:20 @layer core @author han @header-end */

/**
 * Marks a method parameter as a MCP Argument.
 * 在 resource 和 prompt 功能里可以作为参数
 * 在Tool里可以标识为mcp的参数, 比如exchange 和 上下文Map(由于类型擦除, 无法准确注入, 故使用该注解)
 * @author Christian Tzolov
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpArg {

	/**
	 * Argument name.
	 */
	String name() default "";

	/**
	 * Argument description.
	 */
	String description() default "";

	/**
	 * True if this argument is required. false if this argument is optional.
	 */
	boolean required() default false;

}
