package com.ai.plug.core.spec.change;

/**
 * @header-start
 * @module CapabilityChangePushScheduler
 * @keywords 调度包装 Runnable 定时任务 diffAndNotify notifyNow 强制推送
 *           McpToolChangeNotifier 调度器无关
 * @updated 2026-10-28 12:20
 * @purpose 把能力变更通知包装成可交给任意调度器执行的 Runnable
 * @layer core
 * @author han
 * @header-end
 */

/**
 * Runnable wrapper for scheduled capability-change notification. Designed
 * to be invoked from any scheduler (Spring {@code @Scheduled}, custom
 * thread pool, test loop, etc).
 *
 * <h2>用法</h2>
 * <pre>{@code
 *   &#64;Bean
 *   public McpToolChangeNotifier notifier(...) { ... }
 *
 *   &#64;Scheduled(fixedRate = 5_000)  // every 5 seconds
 *   public void pushChange() {
 *       CapabilityChangePushScheduler.wrap(notifier).run();
 *   }
 * }</pre>
 *
 * <p>Returns a {@link Runnable} so the scheduler can be wired to any
 * mechanism. Wrapping also enables future enhancements (e.g. batched
 * notifications, conditional gating) without changing call sites.
 */
public final class CapabilityChangePushScheduler {

    private CapabilityChangePushScheduler() {
    }

    /**
     * Build a {@link Runnable} that calls {@link McpToolChangeNotifier#diffAndNotify()}.
     */
    public static Runnable wrap(McpToolChangeNotifier notifier) {
        if (notifier == null) {
            throw new IllegalArgumentException("notifier is required");
        }
        return notifier::diffAndNotify;
    }

    /**
     * Build a {@link Runnable} that calls {@link McpToolChangeNotifier#notifyNow()}
     * — fire regardless of last-snapshot. Use when the user explicitly
     * wants to push a change (e.g. from a webhook handler).
     */
    public static Runnable wrapForce(McpToolChangeNotifier notifier) {
        if (notifier == null) {
            throw new IllegalArgumentException("notifier is required");
        }
        return notifier::notifyNow;
    }
}