package com.ai.plug.core.context.complete;

/**
 * @header-start
 * @module CompleteContextFactory
 * @keywords 工厂模式 上下文工厂 createCompleteContext ICompleteContext 补全容器 创建入口
 * @updated 2026-10-28 12:20
 * @purpose 以工厂方法创建补全上下文实现，屏蔽具体实现类对外暴露
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/7/14 16:00
 */

public class CompleteContextFactory {
    /**
     * create CompleteContext
     */
    public static ICompleteContext createCompleteContext() {
        return new CompleteContext();
    }
}
