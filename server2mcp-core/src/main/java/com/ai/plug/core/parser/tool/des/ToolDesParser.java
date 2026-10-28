package com.ai.plug.core.parser.tool.des;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;


/**
 * @header-start
 * @module ToolDesParser
 * @keywords 描述解析器 责任链 SpringAI @Tool注解 工具描述 description 兼容原生注解
 *           AbstractDesParser 解析器优先级 order
 * @updated 2026-10-28 12:20
 * @purpose 从 Spring AI 原生 @Tool 注解提取工具描述，兼容非 MCP 专属注解的工具
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * time: 2025/4/29 23:18
 */
public class ToolDesParser extends AbstractDesParser{
    @Override
    public String doDesParse(Method method, Class<?> toolClass) {
        Tool toolAnnotation = method.getAnnotation(Tool.class);

        if (toolAnnotation == null) {
            return null;
        }

        String des = toolAnnotation.description();
        if (StringUtils.hasText(des)) {
            return des;
        }
        return null;
    }
}
