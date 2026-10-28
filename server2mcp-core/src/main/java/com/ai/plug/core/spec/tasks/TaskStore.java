package com.ai.plug.core.spec.tasks;

import java.util.List;
import java.util.Optional;

/**
 * @header-start
 * @module TaskStore
 * @keywords 任务存储契约 register get list update cancel 生命周期
 *             SEP-2663 传输无关 可替换实现
 * @updated 2026-10-28 12:20
 * @purpose 定义任务状态的服务端存储契约，支持替换为分布式实现
 * @layer core
 * @author han
 * @header-end
 */

/**
 * Server-side storage for Tasks (protocol 2026-07-28 SEP-2663).
 *
 * <p>When a tool returns {@link TaskTypes.TaskHandle}, the framework
 * registers the task in the store with status {@link TaskTypes.Status#running}
 * (or {@code pending} if the task hasn't started). Subsequent HTTP
 * endpoints ({@code GET /mcp/tasks/&#123;id&#125;}, {@code POST /mcp/tasks/&#123;id&#125;/cancel})
 * read/update from this store.
 *
 * <p>The interface is transport-agnostic — production deployments may back
 * this with Redis or JDBC. The default in-memory implementation is at
 * {@link InMemoryTaskStore}.
 *
 * @author han
 * @time 2026/8/3
 */
public interface TaskStore {

    /**
     * Register a new task in {@code running} status.
     * @return the registered task snapshot
     */
    TaskTypes.TaskStatus register(TaskTypes.TaskHandle handle);

    /**
     * Look up a task by id. Empty if unknown or already completed+cancelled.
     */
    Optional<TaskTypes.TaskStatus> get(String taskId);

    /**
     * List all live tasks (any status).
     */
    List<TaskTypes.TaskStatus> list();

    /**
     * Update the task's status. Returns empty if the task id is unknown.
     */
    Optional<TaskTypes.TaskStatus> update(String taskId, TaskTypes.TaskStatus status);

    /**
     * Cancel a running task. Idempotent: cancelling an already-cancelled
     * task is a no-op.
     */
    boolean cancel(String taskId, String reason);

    /** Number of live tasks in the store. */
    int activeCount();
}