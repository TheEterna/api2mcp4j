package com.ai.plug.core.parser.tool.param;

import com.ai.plug.core.annotation.McpArg;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @header-start
 * @module McpToolParamParser
 * @keywords 参数解析器 责任链 McpArg 专属注解 参数必填 参数描述
 *           AbstractParamParser 解析器优先级 order
 * @updated 2026-10-28 12:20
 * @purpose 从框架专属 @McpArg 注解提取参数必填标记与描述，是参数解析链最高优先级一环
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/6/30 3:49
 */

public class McpToolParamParser extends AbstractParamParser {
    @Override
    public Boolean doParamRequiredParse(Method method, Class<?> toolClass, int index) {

        Parameter parameter = method.getParameters()[index];
        McpArg toolParamAnnotation = parameter.getAnnotation(McpArg.class);
        if (toolParamAnnotation != null) {
            return toolParamAnnotation.required();
        }

        return null;
    }

    @Override
    public String doParamDesParse(Method method, Class<?> toolClass, int index) {

        Parameter parameter = method.getParameters()[index];

        McpArg toolParamAnnotation = parameter.getAnnotation(McpArg.class);
        if (toolParamAnnotation != null && StringUtils.hasText(toolParamAnnotation.description())) {
            return toolParamAnnotation.description();
        }

        return null;
    }
}