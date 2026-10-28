package com.ai.plug.core.context.complete;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @header-start
 * @module CompleteContext
 * @keywords 补全上下文 注册容器 ICompleteContext ConcurrentHashMap addComplete getRawCompletes
 *           补全注册面 不可变视图 并发集合
 * @updated 2026-10-28 12:20
 * @purpose 以并发集合承载补全注册定义，是补全注册面到暴露面的唯一数据源
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/6/17 0:53
 */

public class CompleteContext implements ICompleteContext{

    private static Set<String> rawCompletes = ConcurrentHashMap.newKeySet();

    CompleteContext() {
    }

    @Override
    public Set<String> getRawCompletes() {
        return Collections.unmodifiableSet(rawCompletes);
    }

    @Override
    public void addComplete(String name) {
        rawCompletes.add(name);
    }




}
