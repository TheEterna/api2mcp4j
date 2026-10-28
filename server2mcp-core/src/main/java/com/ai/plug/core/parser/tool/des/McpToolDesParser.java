package com.ai.plug.core.parser.tool.des;

import com.ai.plug.core.annotation.McpTool;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @header-start
 * @module McpToolDesParser
 * @keywords 描述解析器 责任链 McpTool注解 description 工具描述 最高优先级
 *           AbstractDesParser order=0 框架专属
 * @updated 2026-10-28 12:20
 * @purpose 从框架专属 @McpTool 注解提取工具描述，是描述解析链的最高优先级一环
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/6/30 3:44
 */

public class McpToolDesParser extends AbstractDesParser{
    @Override
    public String doDesParse(Method method, Class<?> toolClass) {
        McpTool toolAnnotation = method.getAnnotation(McpTool.class);

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
