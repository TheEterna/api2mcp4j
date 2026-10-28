package com.ai.plug.core.context.complete;

import java.util.Set;

/** @header-start @module ICompleteContext @keywords 补全上下文接口 getRawCompletes addComplete 注册容器 极简接口 单一出口 @updated 2026-10-28 12:20 @layer core @author han @header-end */

/**
 * @author han
 * @time 2025/7/14 11:34
 */

public interface ICompleteContext {

    Set<String> getRawCompletes();

    void addComplete(String name);

}
