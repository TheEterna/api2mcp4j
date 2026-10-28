package com.ai.plug.core.context.prompt;


/**
 * @header-start
 * @module PromptContextFactory
 * @keywords 工厂模式 上下文工厂 createPromptContext IPromptContext 提示容器 创建入口
 * @updated 2026-10-28 12:20
 * @purpose 以工厂方法创建提示词上下文实现，屏蔽具体实现类对外暴露
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/7/14 11:46
 */

public class PromptContextFactory {

    /**
     * 获取 PromptContext
     */
    public static IPromptContext createPromptContext() {
        return new PromptContext();
    }
}
