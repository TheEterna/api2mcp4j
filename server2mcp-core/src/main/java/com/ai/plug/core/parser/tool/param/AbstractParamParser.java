package com.ai.plug.core.parser.tool.param;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static com.ai.plug.common.constants.PromptConstants.CUSTOM_PARAM_DES;
import static com.ai.plug.common.constants.PromptConstants.DEFAULT_PARAM_DES;

/**
 * @header-start
 * @module AbstractParamParser
 * @keywords 参数解析 责任链 抽象基类 doParamRequiredParse doParamDesParse 参数必填
 *           Nullable 参数描述 JSON Schema AbstractParamParser 解析器SPI 参数解析器链
 * @updated 2026-10-28 12:20
 * @purpose 参数解析器责任链的抽象基类，定义"参数必填性"与"参数描述"的统一解析契约
 * @layer core
 * @affects 改动影响全部 8 个参数解析器（McpTool/Jackson/JavaDoc/Mvc/Swagger3/Swagger2/Tool）的实现契约与生成的 Schema
 * @see McpToolParamParser — order=0 的最高优先级实现
 * @constraint 责任链 SPI：新增/改名抽象方法会波及所有子类与工具入参 Schema 生成
 * @author han
 * @header-end
 */

/**
 * @author: han
 * time: 2025/04/2025/4/1 23:18
 * des:
 */
public abstract class AbstractParamParser {

    /**
     * Tool 的 工具的参数 是否必须 (Required) 的 解析逻辑
     * @param toolMethod 工具方法
     * @param toolClass 工具类
     * @param index 索引, 解析具体方法参数 所以需要具体到某个方法的第几个(索引)
     * @return 返回该参数是否必须(Boolean)
     */
    public abstract Boolean doParamRequiredParse(Method toolMethod, Class<?> toolClass, int index);

    /**
     * Tool 默认的 工具的参数 是否必须 (Required) 的 解析逻辑
     * @param toolMethod 工具方法
     * @param toolClass 工具类
     * @param index 索引, 解析具体方法参数 所以需要具体到某个方法的第几个(索引)
     * @return 返回该参数是否必须(Boolean)
     */
    public static Boolean doDefaultParamRequiredParse(Method toolMethod, Class<?> toolClass, int index) {
        Parameter parameter = toolMethod.getParameters()[index];

        // 默认检查是否必须操作
        Nullable nullableAnnotation = parameter.getAnnotation(Nullable.class);

        return nullableAnnotation == null;
    }

    /**
     * Tool 默认的 工具的参数的描述 的解析逻辑
     * @param toolMethod 工具方法
     * @param toolClass 工具类
     * @param index 索引, 解析具体方法参数 所以需要具体到某个方法的第几个(索引)
     * @return 返回执行解析逻辑后的工具参数描述(String)
     */
    public abstract String doParamDesParse(Method toolMethod, Class<?> toolClass, int index);

    /**
     * Tool 默认的 工具的参数的描述 的解析逻辑
     * @param toolMethod 工具方法
     * @param toolClass 工具类
     * @param index 索引, 解析具体方法参数 所以需要具体到某个方法的第几个(索引)
     * @return 返回执行默认解析逻辑后的工具参数描述(String)
     */
    public static String doDefaultParamDesParse(Method toolMethod, Class<?> toolClass, int index) {
        Parameter parameter = toolMethod.getParameters()[index];

        return CUSTOM_PARAM_DES == null ? DEFAULT_PARAM_DES.apply(parameter) :  CUSTOM_PARAM_DES.apply(parameter);
    }


}
