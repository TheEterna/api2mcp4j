package com.ai.plug.core.parser.tool.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @header-start
 * @module JacksonParamParser
 * @keywords 参数解析器 责任链 Jackson JsonProperty JsonPropertyDescription 参数必填
 *           参数描述 AbstractParamParser 解析器优先级
 * @updated 2026-10-28 12:20
 * @purpose 从 Jackson 系列注解提取参数必填标记与参数描述，是参数解析链的一环
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * time: 2025/4/27 8:57
 */
public class JacksonParamParser extends AbstractParamParser {


    @Override
    public Boolean doParamRequiredParse(Method method, Class<?> toolClass, int index) {
        Parameter parameter = method.getParameters()[index];

        // 这个是 映射的逻辑，它是首位，如果这个参数json都决定不映射，那就不映射
        JsonProperty propertyAnnotation = parameter.getAnnotation(JsonProperty.class);
        if (propertyAnnotation != null) {
            return propertyAnnotation.required();
        }
        return null;
    }

    @Override
    public String doParamDesParse(Method method, Class<?> toolClass, int index) {
        Parameter parameter = method.getParameters()[index];

        JsonPropertyDescription jsonPropertyDescriptionAnnotation = parameter.getAnnotation(JsonPropertyDescription.class);
        if (jsonPropertyDescriptionAnnotation != null && StringUtils.hasText(jsonPropertyDescriptionAnnotation.value())) {
            return jsonPropertyDescriptionAnnotation.value();
        }

        return null;
    }
}
