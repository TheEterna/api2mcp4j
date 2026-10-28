package com.ai.plug.core.spec.tasks;

import com.ai.plug.common.utils.JsonParser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @header-start
 * @module TasksEndpoint
 * @keywords 任务端点 HTTP契约 handleGet handleList handleCancel JSON序列化
 *             TaskStore 传输层挂载
 * @updated 2026-10-28 12:20
 * @purpose 定义任务操作的 HTTP 层端点契约，覆盖查询、列举与取消三类动作
 * @layer core
 * @author han
 * @header-end
 */

/**
 * Framework-layer HTTP endpoint contract for {@code tasks/*}.
 *
 * <p>SDK 2.0 has no JSON-RPC schema for the Tasks extension, so the
 * framework exposes task operations at the HTTP layer:
 * <ul>
 *   <li>{@code GET /mcp/tasks/&#123;id&#125;} → {@link #handleGet};</li>
 *   <li>{@code GET /mcp/tasks} → {@link #handleList};</li>
 *   <li>{@code POST /mcp/tasks/&#123;id&#125;/cancel} → {@link #handleCancel}.</li>
 * </ul>
 *
 * <p>Downstream transports mount these contracts directly.
 *
 * @author han
 * @time 2026/8/3
 */
public final class TasksEndpoint {

    private final TaskStore store;

    public TasksEndpoint(TaskStore store) {
        if (store == null) {
            throw new IllegalArgumentException("store is required");
        }
        this.store = store;
    }

    /** Get a single task status. Returns empty map (with
     *  {@code found=false} flag) if the task is unknown. */
    public Map<String, Object> handleGet(String taskId) {
        Optional<TaskTypes.TaskStatus> status = store.get(taskId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("found", status.isPresent());
        status.ifPresent(s -> body.put("task", s));
        return body;
    }

    /** List all live tasks. */
    public Map<String, Object> handleList() {
        List<TaskTypes.TaskStatus> tasks = store.list();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("count", tasks.size());
        body.put("tasks", tasks);
        return body;
    }

    /** Cancel a task. Returns success/failure with the updated status. */
    public Map<String, Object> handleCancel(String taskId, String reason) {
        boolean cancelled = store.cancel(taskId, reason);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cancelled", cancelled);
        body.put("taskId", taskId);
        store.get(taskId).ifPresent(s -> body.put("task", s));
        return body;
    }

    /** Convenience: serialize {@link #handleGet} as JSON. */
    public String handleGetJson(String taskId) throws java.io.IOException {
        return JsonParser.getObjectMapper().writeValueAsString(handleGet(taskId));
    }

    /** Convenience: serialize {@link #handleList} as JSON. */
    public String handleListJson() throws java.io.IOException {
        return JsonParser.getObjectMapper().writeValueAsString(handleList());
    }

    /** Convenience: serialize {@link #handleCancel} as JSON. */
    public String handleCancelJson(String taskId, String reason) throws java.io.IOException {
        return JsonParser.getObjectMapper().writeValueAsString(handleCancel(taskId, reason));
    }
}