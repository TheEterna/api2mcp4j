package com.ai.plug.autoconfigure.annotation;

import java.lang.annotation.*;

/** @header-start @module ToolNotScanForAuto @keywords 排除标记注解 自动扫描排除 类级排除 方法级排除 interface作用域 扫描过滤 排除机制 @updated 2026-10-28 12:20 @layer autoconfigure @author han @header-end */

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolNotScanForAuto {
}
