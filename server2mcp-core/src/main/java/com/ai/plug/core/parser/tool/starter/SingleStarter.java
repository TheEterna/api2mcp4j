package com.ai.plug.core.parser.tool.starter;

import com.ai.plug.core.parser.tool.des.AbstractDesParser;
import com.ai.plug.core.parser.tool.param.AbstractParamParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;


/**
 * @header-start
 * @module SingleStarter
 * @keywords 解析编排 首个命中即用 runDesParse runParamRequiredParse runParamDesParse
 *           AbstractStarter 解析器链 兜底默认解析 findFirst
 * @updated 2026-10-28 12:45
 * @purpose 按解析器 order 顺序取首个命中结果，全不命中时回退默认解析
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * time: 2025/4/29 17:03
 */
@Slf4j
public final class SingleStarter extends AbstractStarter {


    @Override
    public String runDesParse(List<AbstractDesParser> parserList, Method method, Class<?> toolClass) {

        String result = parserList
                .stream()
                .map((parser) -> parser.doDesParse(method, toolClass))
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);

        if (!StringUtils.hasText(result)) {
            // 常规兜底路径：所有已配置解析器都没给出描述，按设计回退默认解析（方法名驼峰拆分）。
            // 这是「没配置到能解析的描述源」，不是错误——降为 debug，避免每个工具方法给下游刷 WARN。
            log.debug("工具 {} 的描述未被任何已配置解析器命中，已回退默认解析（方法名驼峰拆分）", toolClass.getName() + "-" + method.getName());
            return AbstractDesParser.doDefaultParse(method, toolClass);
        }
        return result;

    }

    @Override
    public Boolean runParamRequiredParse(List<AbstractParamParser> parserList, Method method, Class<?> toolClass, int index) {

        Boolean result = parserList
                .stream()
                .map((parser) -> parser.doParamRequiredParse(method, toolClass, index))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (result == null) {
            // 常规兜底路径：所有已配置解析器都没判定必填性，按设计回退「默认必填」。
            log.debug("工具 {} 第 {} 个参数的必填性未被任何已配置解析器命中，已回退默认判定（必填）", toolClass.getName() + "-" + method.getName(), index);
            return AbstractParamParser.doDefaultParamRequiredParse(method, toolClass, index);
        }
        return result;
    }

    @Override
    public String runParamDesParse(List<AbstractParamParser> parserList, Method method, Class<?> toolClass, int index) {
        String result = parserList
                .stream()
                .map((parser) -> parser.doParamDesParse(method, toolClass, index))
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);

        if (!StringUtils.hasText(result)) {
            // 常规兜底路径：所有已配置解析器都没给出参数描述，按设计回退默认描述。
            log.debug("工具 {} 第 {} 个参数的描述未被任何已配置解析器命中，已回退默认描述", toolClass.getName() + "-" + method.getName(), index);
            return AbstractParamParser.doDefaultParamDesParse(method, toolClass, index);
        }

        return result;

    }


}
