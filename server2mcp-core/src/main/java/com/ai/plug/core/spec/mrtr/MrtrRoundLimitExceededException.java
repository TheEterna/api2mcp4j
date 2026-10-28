package com.ai.plug.core.spec.mrtr;

/**
 * @header-start
 * @module MrtrRoundLimitExceededException
 * @keywords MRTR轮次超限 异常 会话遗弃 requestState 上限值
 *               MrtrSafetyLimits 运行时异常
 * @updated 2026-10-28 12:20
 * @purpose 在 MRTR 会话轮次超过安全上限时抛出，并携带会话令牌与上限值
 * @layer core
 * @author han
 * @header-end
 */

/**
 * Thrown by {@link MrtrDriver#resume} when a conversation exceeds
 * {@link MrtrSafetyLimits#maxRounds()}. The session is abandoned (removed
 * from the store) before the exception is raised, so the next call with
 * the same {@code requestState} will see an "unknown token" error and
 * can start a fresh session.
 *
 * <p>Tools should catch this and translate to a user-friendly error
 * (e.g. "Too many follow-up questions; please contact support").
 *
 * @author han
 * @time 2026/8/3
 */
public class MrtrRoundLimitExceededException extends RuntimeException {

    private final String requestState;
    private final int limit;

    public MrtrRoundLimitExceededException(String requestState, int limit) {
        super("MRTR round limit exceeded for session " + requestState
            + " (limit=" + limit + "). Session abandoned.");
        this.requestState = requestState;
        this.limit = limit;
    }

    public String requestState() {
        return requestState;
    }

    public int limit() {
        return limit;
    }
}