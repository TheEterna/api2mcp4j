package com.ai.plug.core.annotation;

import com.ai.plug.core.register.prompt.McpPromptScanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/** @header-start @module McpPromptScans @keywords 可重复注解容器 提示词扫描 RepeatingRegistrar @Import McpPromptScan value 组合扫描 @updated 2026-10-28 12:20 @layer core @author han @header-end */

/**
 * @author 韩
 * time: 2025/6/13 3:37
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({McpPromptScanRegistrar.RepeatingRegistrar.class})
public @interface McpPromptScans {
    McpPromptScan[] value();
}
