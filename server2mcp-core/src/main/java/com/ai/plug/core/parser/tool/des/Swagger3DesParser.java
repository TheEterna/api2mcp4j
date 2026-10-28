package com.ai.plug.core.parser.tool.des;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @header-start
 * @module Swagger3DesParser
 * @keywords 描述解析器 责任链 Swagger3 OpenAPI3 Operation 工具描述 summary description 拼装
 *           AbstractDesParser 解析器优先级 order
 * @updated 2026-10-28 12:20
 * @purpose 从 Swagger3 的 Operation 注解拼装工具描述，是描述解析链的一环
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * time: 2025/4/24 3:43
 */

public class Swagger3DesParser extends AbstractDesParser {

    @Override
    public String doDesParse(Method method, Class<?> toolClass) {
        StringBuilder result = new StringBuilder();

        Operation operationAnnotation = method.getAnnotation(Operation.class);
        if (operationAnnotation == null) {
            return null;
        }
        String simpleDes = operationAnnotation.summary();

        String detailedDes = operationAnnotation.description();

        // 如果有值
        if (StringUtils.hasText(simpleDes)) {
            result.append(simpleDes);
        }

        if (StringUtils.hasText(detailedDes)) {
            // 如果之前已经有了一个简单描述了, 就加一个回车
            if (StringUtils.hasText(result)) {
                result.append('\n');
            }
            result.append(detailedDes);
        }

        // todo 可以加一个判断如果过时 就一句 (已过时) 提示词

        return result.toString().trim().isBlank() ? null : result.toString().trim();
    }
}
