package com.ai.plug.core.spec.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @header-start
 * @module InMemoryAugmentedPromptStore
 * @keywords 增强提示内存实现 CopyOnWriteArrayList ConcurrentHashMap 索引清理
 *               drain 线程安全 单实例部署
 * @updated 2026-10-28 12:20
 * @purpose 以并发容器承载任务增强提示队列，是默认的内存存储实现
 * @layer core
 * @author han
 * @header-end
 */

/**
 * Default in-memory {@link AugmentedPromptStore}. Thread-safe via
 * per-task {@link CopyOnWriteArrayList} + {@link ConcurrentHashMap};
 * suitable for single-instance deployments.
 *
 * @author han
 * @time 2026/8/3
 */
public final class InMemoryAugmentedPromptStore implements AugmentedPromptStore {

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<AugmentedPrompt>> byTaskId
        = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> promptIdToTaskId = new ConcurrentHashMap<>();

    @Override
    public void add(AugmentedPrompt prompt) {
        if (prompt == null) {
            throw new IllegalArgumentException("prompt is required");
        }
        byTaskId.computeIfAbsent(prompt.taskId(), k -> new CopyOnWriteArrayList<>())
            .add(prompt);
        promptIdToTaskId.put(prompt.promptId(), prompt.taskId());
    }

    @Override
    public List<AugmentedPrompt> list(String taskId) {
        if (taskId == null) return List.of();
        CopyOnWriteArrayList<AugmentedPrompt> bucket = byTaskId.get(taskId);
        return bucket == null ? List.of() : new ArrayList<>(bucket);
    }

    @Override
    public Optional<AugmentedPrompt> get(String promptId) {
        if (promptId == null) return Optional.empty();
        String taskId = promptIdToTaskId.get(promptId);
        if (taskId == null) return Optional.empty();
        return byTaskId.get(taskId).stream()
            .filter(p -> p.promptId().equals(promptId))
            .findFirst();
    }

    @Override
    public List<AugmentedPrompt> drain(String taskId) {
        if (taskId == null) return List.of();
        CopyOnWriteArrayList<AugmentedPrompt> bucket = byTaskId.remove(taskId);
        if (bucket == null) return List.of();
        // Clean up the id index
        for (AugmentedPrompt p : bucket) {
            promptIdToTaskId.remove(p.promptId());
        }
        return new ArrayList<>(bucket);
    }

    @Override
    public int activeCount() {
        return promptIdToTaskId.size();
    }
}