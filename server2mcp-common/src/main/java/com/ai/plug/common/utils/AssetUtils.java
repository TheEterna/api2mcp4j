package com.ai.plug.common.utils;

import org.slf4j.Logger;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @header-start
 * @module AssetUtils
 * @keywords 工具类 资产判定 isFunctionalType 函数式返回类型 Function Supplier Consumer
 *           不受支持的工具方法 ClassUtils 扫描期防护 警告日志 忽略
 * @updated 2026-10-28 12:20
 * @purpose 判定工具方法是否返回函数式类型，供扫描期跳过不受支持的 MCP 工具
 * @layer common
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/6/30 2:46
 */

public class AssetUtils {


    public static boolean isFunctionalType(Method toolMethod, Logger log) {
        boolean isFunction = ClassUtils.isAssignable(Function.class, toolMethod.getReturnType())
                || ClassUtils.isAssignable(Supplier.class, toolMethod.getReturnType())
                || ClassUtils.isAssignable(Consumer.class, toolMethod.getReturnType());

        if (isFunction) {
            log.warn("Method {} is a McpTool but returns a functional type. "
                    + "This is not supported and the method will be ignored.", toolMethod.getName());
        }

        return isFunction;
    }

}
