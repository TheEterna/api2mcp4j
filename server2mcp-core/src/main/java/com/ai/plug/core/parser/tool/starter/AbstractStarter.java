package com.ai.plug.core.parser.tool.starter;

import com.ai.plug.core.parser.tool.des.AbstractDesParser;
import com.ai.plug.core.parser.tool.param.AbstractParamParser;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @header-start
 * @module AbstractStarter
 * @keywords 解析编排 抽象基类 runDesParse runParamRequiredParse runParamDesParse
 *           AbstractDesParser AbstractParamParser 责任链掌控者 策略模式 解析器编排
 * @updated 2026-10-28 12:20
 * @purpose 抽象出「编排解析器链」的统一契约，决定描述与参数解析结果如何被汇总
 * @layer core
 * @affects 改动影响描述与参数两条解析链路的汇总策略，以及全部 Starter 实现与调用方
 * @see SingleStarter — 当前生效的实现，首个命中即返回
 * @constraint 解析链编排核心：改抽象方法会波及全部 Starter 实现与解析结果默认值回退逻辑
 * @author han
 * @header-end
 */

/**
 * 整体逻辑: 每个解析器 解析出来的 数据是一定的, starter 作为一个掌控者, 对解析器 进行编排
 * time: 2025/4/29 17:02
 * @author 她也曾美如画
 */

public abstract class AbstractStarter {


    /**
     * 编排解析工具描述的解析逻辑
     * @param parserList 解析器列表
     * @param toolMethod 工具的方法
     * @param toolClass 工具的类
     * @return 解析后的工具注释
     */
    public abstract String runDesParse(List<AbstractDesParser> parserList, Method toolMethod, Class<?> toolClass);

    /**
     * 解析工具参数是否必须
     * @param parserList 解析器列表
     * @param toolMethod 工具的方法
     * @param toolClass 工具的类
     * @param index 参数 索引
     * @return 工具参数是否必须
     */
    public abstract Boolean runParamRequiredParse(List<AbstractParamParser> parserList, Method toolMethod, Class<?> toolClass, int index);

    /**
     * 解析工具参数的描述
     * @param parserList 解析器列表
     * @param toolMethod 工具的方法
     * @param toolClass 工具的类
     * @param index 参数 索引
     * @return 工具参数的描述
     */
    public abstract String runParamDesParse(List<AbstractParamParser> parserList, Method toolMethod, Class<?> toolClass, int index);

}
