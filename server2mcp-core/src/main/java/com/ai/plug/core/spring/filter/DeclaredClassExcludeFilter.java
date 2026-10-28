package com.ai.plug.core.spring.filter;

import org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter;

import java.util.HashSet;
import java.util.Set;

/**
 * @header-start
 * @module DeclaredClassExcludeFilter
 * @keywords 类型过滤器 排除过滤器 AbstractTypeHierarchyTraversingFilter 声明类排除
 *           scanFilter 排除扫描 类名匹配 框架类不扫描 继承与接口开关
 * @updated 2026-10-28 12:20
 * @purpose 按声明类名单排除扫描候选，避免框架基类被当作工具注册
 * @layer core
 * @author han
 * @header-end
 */

public class DeclaredClassExcludeFilter extends AbstractTypeHierarchyTraversingFilter {
    private final Set<String> classNames = new HashSet<>();

    public DeclaredClassExcludeFilter(boolean considerInherited, boolean considerInterfaces, Class<?>... sources) {
        super(considerInherited, considerInterfaces);
        for (Class<?> source : sources) {
            this.classNames.add(source.getName());
        }
    }

    public DeclaredClassExcludeFilter(Class<?>... sources) {
        super(false, false);

        for (Class<?> source : sources) {
            this.classNames.add(source.getName());
        }

    }

    public boolean matchClassName(String className) {
        return this.classNames.contains(className);
    }
}