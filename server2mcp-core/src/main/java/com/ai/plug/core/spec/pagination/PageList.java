package com.ai.plug.core.spec.pagination;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @header-start
 * @module PageList
 * @keywords 分页列表 record items totalItems 下一游标 元数据提取
 *           McpPaging 工具返回类型 分页输出
 * @updated 2026-10-28 12:20
 * @purpose 以 record 定义工具方法的分页返回类型，承载条目与总数并支持推导下一游标
 * @layer core
 * @author han
 * @header-end
 */

/**
 * Generic paged-list return type for tool methods — lets the framework
 * extract pagination metadata (nextCursor, totalItems) without the tool
 * method having to manually encode it into a {@link McpSchema} type.
 *
 * <h2>用法</h2>
 * <pre>{@code
 *   &#64;Tool
 *   public PageList&lt;Row&gt; listUsers(McpPaging paging) {
 *       int total = db.count();
 *       List&lt;Row&gt; items = db.fetchSlice(paging.offset(), paging.size());
 *       return PageList.of(items, total);  // nextCursor auto-computed
 *   }
 * }</pre>
 *
 * <p>{@code PageList} holds the items + a hint of the total collection size.
 * When the converter sees a {@code PageList} return value, it:
 * <ol>
 *   <li>Serializes {@code items} as the call-tool result content;</li>
 *   <li>Computes {@code nextCursor} from {@link #totalItems()} via
 *       {@link McpPaging#nextCursor(int)};</li>
 *   <li>Emits both as meta fields so {@link McpResultWriter} / wire JSON
 *       consumers can pick them up.</li>
 * </ol>
 *
 * @author han
 * @time 2026/8/1 05:49
 */
public record PageList<T>(List<T> items, int totalItems) {

    public PageList {
        if (items == null) {
            throw new IllegalArgumentException("items is required");
        }
        if (totalItems < 0) {
            throw new IllegalArgumentException("totalItems must be >= 0, got: " + totalItems);
        }
    }

    public static <T> PageList<T> of(List<T> items, int totalItems) {
        return new PageList<>(items, totalItems);
    }

    /** Empty page (zero items, zero total). */
    public static <T> PageList<T> empty() {
        return new PageList<>(List.of(), 0);
    }

    /**
     * Compute nextCursor for this page given a paging context. Returns null
     * when this is the last page (no more items to fetch).
     */
    @Nullable
    public String nextCursor(McpPaging paging) {
        return paging.nextCursor(totalItems);
    }
}