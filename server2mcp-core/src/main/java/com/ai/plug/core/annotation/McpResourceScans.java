package com.ai.plug.core.annotation;

import com.ai.plug.core.register.resource.McpResourceScanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/** @header-start @module McpResourceScans @keywords 可重复注解容器 资源扫描 RepeatingRegistrar @Import McpResourceScan value 组合扫描 @updated 2026-10-28 12:20 @layer core @author han @header-end */

/**
 * @author 韩
 * time: 2025/6/3 3:18
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({McpResourceScanRegistrar.RepeatingRegistrar.class})
public @interface McpResourceScans {

    McpResourceScan[] value();

}
