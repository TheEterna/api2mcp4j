package com.ai.plug.core.annotation;

import com.ai.plug.core.register.tool.McpToolScanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/** @header-start @module ToolScans @keywords 可重复注解容器 工具扫描 RepeatingRegistrar @Import ToolScan value 组合扫描 @updated 2026-10-28 12:20 @layer core @author han @header-end */

/**
 * @author: han
 * time: 2025/04/2025/4/2 22:29
 * des:
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({McpToolScanRegistrar.RepeatingRegistrar.class})
public @interface ToolScans {

    ToolScan[] value();

}
