/*
 * Copyright 2025-2025 the original author or authors.
 */

package com.ai.plug.core.annotation;

import java.lang.annotation.*;

/** @header-start @module McpPrompt @keywords 提示词注解 MCP Prompt 提示名称 描述信息 方法标记 LLM提示 提示模板 @updated 2026-10-28 12:20 @layer core @author han @header-end */

/**
 * Marks a method as a MCP Prompt.
 *
 * @author Christian Tzolov
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpPrompt {

	/**
	 * A human-readable name for this resource. This can be used by clients to populate UI
	 * elements.
	 */
	String name() default "";

	/**
	 * 该资源的描述信息，用于帮助客户端提升LLM对可用资源的理解。可以将其视为给模型的一个“提示”。
	 */
	String description() default "";

}
