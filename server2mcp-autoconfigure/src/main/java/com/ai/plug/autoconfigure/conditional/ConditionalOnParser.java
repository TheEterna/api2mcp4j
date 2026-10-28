package com.ai.plug.autoconfigure.conditional;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** @header-start @module ConditionalOnParser @keywords 条件注解 解析器开关 ConditionalOnParser Conditions.ParserCondition 参数解析器 类型开关 plugin.mcp.parser 条件装配 @updated 2026-10-28 12:20 @layer autoconfigure @author han @header-end */

/**
 * @author 韩
 * time: 2025/5/16 12:37
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(Conditions.ParserCondition.class)
public @interface ConditionalOnParser {

    Class<?> type();

    String value();
}
