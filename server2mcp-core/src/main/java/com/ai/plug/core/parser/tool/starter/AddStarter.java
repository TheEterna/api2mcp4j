package com.ai.plug.core.parser.tool.starter;

import com.ai.plug.core.parser.tool.des.AbstractDesParser;
import com.ai.plug.core.parser.tool.param.AbstractParamParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;


/**
 * @header-start
 * @module AddStarter
 * @keywords 解析编排 多解析器累加 已废弃 @Deprecated runDesParse 描述拼接
 *           AbstractStarter 解析器链 兜底默认解析
 * @updated 2026-10-28 12:20
 * @purpose 把全部描述解析器的结果累加拼接，因语义不佳已废弃，仅作兼容保留
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * time: 2025/4/29 17:02
 */
@Slf4j
@Deprecated
public class AddStarter extends AbstractStarter {


    @Override
    public String runDesParse(List<AbstractDesParser> parserList, Method method, Class<?> toolClass) {
        StringBuilder result = new StringBuilder();

        parserList.forEach((parser) -> {
            result.append(parser.doDesParse(method, toolClass));
        });

        // 没解析的话，就执行默认操作
        if (!StringUtils.hasText(result.toString())) {
            log.warn("Please note that the parsing module you specified resulted in the {} tool not parsing the tool parameters", toolClass.getName() + "-" + method.getName());
            return AbstractDesParser.doDefaultParse(method, toolClass);
        }
        return result.toString();
    }

    @Override
    public Boolean runParamRequiredParse(List<AbstractParamParser> parserList, Method method, Class<?> toolClass, int index) {
        return null;
    }

    @Override
    public String runParamDesParse(List<AbstractParamParser> parserList, Method method, Class<?> toolClass, int index) {
        return null;
    }


}



