package com.ai.plug.core.context.prompt;

import java.util.Set;

/** @header-start @module IPromptContext @keywords 提示词上下文接口 getRawPrompts addPrompt 注册容器 极简接口 单一出口 @updated 2026-10-28 12:20 @layer core @author han @header-end */

/**
 * @author han
 * @time 2025/7/14 11:34
 */

public interface IPromptContext {

    /**
     * 获取原始的prompt
     */
    Set<String> getRawPrompts();


    /**
     * 添加prompt
     */
    void addPrompt(String name);

}
