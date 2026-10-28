package com.ai.plug.core.parser.tool.param;

import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @header-start
 * @module ToolParamParser
 * @keywords 参数解析器 责任链 SpringAI ToolParam 参数必填 参数描述
 *           AbstractParamParser 兼容原生注解
 * @updated 2026-10-28 12:20
 * @purpose 从 Spring AI 原生 @ToolParam 注解提取参数必填标记与描述
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * time: 2025/4/30 2:56
 */
public class ToolParamParser extends AbstractParamParser {
    @Override
    public Boolean doParamRequiredParse(Method method, Class<?> toolClass, int index) {

        Parameter parameter = method.getParameters()[index];
        ToolParam toolParamAnnotation = parameter.getAnnotation(ToolParam.class);
        if (toolParamAnnotation != null) {
            return toolParamAnnotation.required();
        }

        return null;
    }

    @Override
    public String doParamDesParse(Method method, Class<?> toolClass, int index) {

        Parameter parameter = method.getParameters()[index];

        ToolParam toolParamAnnotation = parameter.getAnnotation(ToolParam.class);
        if (toolParamAnnotation != null && StringUtils.hasText(toolParamAnnotation.description())) {
            return toolParamAnnotation.description();
        }

        return null;
    }
}