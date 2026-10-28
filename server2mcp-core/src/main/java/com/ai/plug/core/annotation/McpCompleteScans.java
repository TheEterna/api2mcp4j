package com.ai.plug.core.annotation;

import com.ai.plug.core.register.complete.McpCompleteScanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/** @header-start @module McpCompleteScans @keywords 可重复注解容器 补全扫描 RepeatingRegistrar @Import McpCompleteScan value 组合扫描 @updated 2026-10-28 12:20 @layer core @author han @header-end */

/**
 * @author han
 * @time 2025/6/16 17:06
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({McpCompleteScanRegistrar.RepeatingRegistrar.class})
public @interface McpCompleteScans {
    McpCompleteScan[] value();
}
