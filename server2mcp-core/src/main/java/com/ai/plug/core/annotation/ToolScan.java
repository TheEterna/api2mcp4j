package com.ai.plug.core.annotation;

import com.ai.plug.core.register.tool.McpToolScanRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


/** @header-start @module ToolScan @keywords 工具扫描注解 包扫描 basePackages includeFilters excludeFilters includeToolFilters excludeToolFilters ToolFilter FilterType CLASS ANNOTATION META_ANNOTATION Import Registrar 两级过滤 @updated 2026-10-28 12:20 @layer core @author han @header-end */

/**
 * @author: han
 * time: 2025/04/2025/4/2 22:29
 * des:
 */

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(ToolScans.class)
@Import(McpToolScanRegistrar.class)
public @interface ToolScan {

    @AliasFor("basePackages")
    String[] value() default {};

    @AliasFor("value")
    String[] basePackages() default {};

    Filter[] includeFilters() default {};

    Filter[] excludeFilters() default {};

    ToolFilter[] excludeToolFilters() default {};
    ToolFilter[] includeToolFilters() default {};

    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    public @interface Filter {

        FilterType type() default FilterType.CLASS;

        @AliasFor("classes")
        Class<?>[] value() default {};

        @AliasFor("value")
        Class<?>[] classes() default {};

    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    public @interface ToolFilter {

        ToolFilterType type() default ToolFilterType.ANNOTATION;

        @AliasFor("classes")
        Class<?>[] value() default {};

        @AliasFor("value")
        Class<?>[] classes() default {};

    }

    /**
     * resource 扫描的类 的过滤方式目前有
     * CLASS 按类扫描
     * ANNOTATION 按注解扫描
     */
    enum FilterType {
        /**
         * CLASS 按类扫描
         */
        CLASS,

        /**
         * ANNOTATION 按注解扫描
         */
        ANNOTATION;
    }


    /**
     * resource 扫描具体工具 的过滤方式目前有
     * META_ANNOTATION 按元注解扫描
     * ANNOTATION 按注解扫描
     */
    enum ToolFilterType {
        /**
         * ANNOTATION 按注解扫描
         */
        ANNOTATION,

        /**
         * META_ANNOTATION 按元注解扫描
         */
        META_ANNOTATION;
    }


}
