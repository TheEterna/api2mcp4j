package com.ai.plug.core.parser.tool.des;

import org.springframework.ai.util.ParsingUtils;

import java.lang.reflect.Method;

/**
 * @header-start
 * @module AbstractDesParser
 * @keywords 描述解析 责任链 抽象基类 doDesParse 工具描述 description ParsingUtils
 *           驼峰拆分 默认解析 AbstractDesParser 解析器SPI 描述解析器链 order优先级
 * @updated 2026-10-28 12:20
 * @purpose 描述解析器责任链的抽象基类，定义"从方法与目标类解析出工具描述"的统一契约
 * @layer core
 * @affects 改动影响全部 6 个描述解析器（McpTool/Jackson/JavaDoc/Swagger3/Swagger2/Tool）的实现契约与调用时序
 * @see McpToolDesParser — order=0 的最高优先级实现
 * @constraint 责任链 SPI：新增/改名抽象方法会波及所有子类与 Spring 装配顺序
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * 需要重新getName
 */
public abstract class AbstractDesParser {

    /**
     * 默认Tool 的 工具描述(description)解析逻辑
     * @param toolMethod 工具方法
     * @param toolClass 工具类
     * @return 返回执行默认解析逻辑后的工具描述(String)
     */
    public static String doDefaultParse(Method toolMethod, Class<?> toolClass) {
        return ParsingUtils.reConcatenateCamelCase(toolMethod.getName(), " ");
    }
    /**
     * Tool 的 工具描述(description)解析逻辑, 供子类重写
     * @param method 工具方法
     * @param toolClass 工具类
     * @return 返回执行解析逻辑后的工具描述(String)
     */
    public abstract String doDesParse(Method method, Class<?> toolClass) ;


}
