package com.ai.plug.common.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @header-start
 * @module CommonUtil
 * @keywords 工具类 方法全限定名 getFullyQualifiedName 反射 Method Parameter 签名拼接
 *           类名 方法名 参数类型列表 唯一标识
 * @updated 2026-10-28 12:20
 * @purpose 拼接方法的全限定签名（类名+方法名+参数类型），用于唯一标识一个工具方法
 * @layer common
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * time: 2025/5/7 22:59
 */

public class CommonUtil {

//    public static Boolean isOK

    public static String getFullyQualifiedName(Method method) {
        // 获取类的全限定名
        String className = method.getDeclaringClass().getName();

        // 获取方法名
        String methodName = method.getName();

        // 构建参数类型列表
        StringBuilder params = new StringBuilder();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            params.append(parameters[i].getType().getName());
            if (i < parameters.length - 1) {
                params.append(",");
            }
        }

        // 组合完整格式
        return className + "." + methodName + "(" + params + ")";
    }}
