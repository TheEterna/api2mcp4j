package com.ai.plug.core.context.prompt;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @header-start
 * @module PromptContext
 * @keywords 提示词上下文 注册容器 IPromptContext ConcurrentHashMap addPrompt getRawPrompts
 *           提示注册面 并发集合
 * @updated 2026-10-28 12:20
 * @purpose 以并发集合承载提示词注册定义，是提示词注册面到暴露面的唯一数据源
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * time: 2025/6/13 11:08
 */

@Slf4j
public class PromptContext implements IPromptContext{

    private Set<String> rawPrompts = ConcurrentHashMap.newKeySet();

    PromptContext() {
    }

    @Override
    public Set<String> getRawPrompts() {
        return rawPrompts;
    }

    @Override
    public void addPrompt(String name) {
        rawPrompts.add(name);
    }





}
