package com.ai.plug.core.parser.tool.des;

import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @header-start
 * @module Swagger2DesParser
 * @keywords 描述解析器 责任链 Swagger2 ApiOperation 工具描述 summary notes 拼装
 *           AbstractDesParser 解析器优先级 order
 * @updated 2026-10-28 12:20
 * @purpose 从 Swagger2 的 ApiOperation 注解拼装工具描述，是描述解析链的一环
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * time: 2025/4/23 10:54
 */

public class Swagger2DesParser extends AbstractDesParser {


    @Override
    public String doDesParse(Method method, Class<?> toolClass) {

        StringBuilder result = new StringBuilder();
        // 显示获取swagger的方法注解
        ApiOperation apiOperationAnnotation = method.getAnnotation(ApiOperation.class);

        if (apiOperationAnnotation == null) {
            return null;
        }

        String simpleDes = apiOperationAnnotation.value();

        String detailedDes = apiOperationAnnotation.notes();

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
