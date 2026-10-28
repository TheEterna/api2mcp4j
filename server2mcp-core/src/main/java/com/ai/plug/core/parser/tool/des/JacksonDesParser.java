package com.ai.plug.core.parser.tool.des;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @header-start
 * @module JacksonDesParser
 * @keywords 描述解析器 责任链 JsonPropertyDescription 工具描述 description 注解解析
 *           AbstractDesParser 解析器优先级 order
 * @updated 2026-10-28 12:20
 * @purpose 从方法上的 JsonPropertyDescription 注解提取工具描述，是描述解析链的一环
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * time: 2025/5/16 10:27
 */

public class JacksonDesParser extends AbstractDesParser {

    @Override
    public String doDesParse(Method method, Class<?> toolClass) {
        JsonPropertyDescription jsonPropertyDescriptionAnnotation = method.getAnnotation(JsonPropertyDescription.class);

        if (jsonPropertyDescriptionAnnotation == null) {
            return null;
        }
        if (StringUtils.hasText(jsonPropertyDescriptionAnnotation.value())) {
            return jsonPropertyDescriptionAnnotation.value();
        }
        return null;

    }
}
