package com.ai.plug.core.spec.mrtr;

/**
 * @header-start
 * @module MrtrSafetyLimits
 * @keywords 安全上限 最大轮次 系统属性 plugin.mcp.mrtr.maxRounds 测试覆写
 *              无限循环防护 运行期旋钮
 * @updated 2026-10-28 12:20
 * @purpose 定义 MRTR 会话的运行期安全上限，防止客户端无限重试拖垮服务端
 * @layer core
 * @author han
 * @header-end
 */

/**
 * Operational safety knobs for MRTR (Multi Round-Trip Request) sessions.
 *
 * <p>Loaded once at startup from system properties; downstream code should
 * use the static {@link #maxRounds()} accessor rather than reading the
 * property directly so tests can override via {@link #setMaxRoundsForTests(int)}.
 *
 * <h2>Knobs</h2>
 * <ul>
 *   <li>{@code plugin.mcp.mrtr.maxRounds} (default {@value #DEFAULT_MAX_ROUNDS})
 *       — maximum number of MRTR rounds before the driver forcibly abandons
 *       the session and throws {@link MrtrRoundLimitExceededException}.
 *       Guards against misbehaving clients (or malicious) that retry
 *       indefinitely with partial inputs.</li>
 * </ul>
 *
 * @author han
 * @time 2026/8/3
 */
public final class MrtrSafetyLimits {

    /** Default maximum rounds before force-abandon. Tuned to be high enough
     *  for legitimate use cases (collecting address → payment → shipping
     *  preference → confirmation) but low enough to abort infinite loops. */
    public static final int DEFAULT_MAX_ROUNDS = 8;

    /** System property key for the max-rounds knob. */
    public static final String MAX_ROUNDS_PROPERTY = "plugin.mcp.mrtr.maxRounds";

    private static volatile int testOverride = -1;

    private MrtrSafetyLimits() {
    }

    /** Current effective max-rounds setting. */
    public static int maxRounds() {
        int override = testOverride;
        if (override >= 0) return override;
        String property = System.getProperty(MAX_ROUNDS_PROPERTY);
        if (property != null) {
            try {
                int parsed = Integer.parseInt(property);
                if (parsed > 0) return parsed;
            } catch (NumberFormatException ignored) {
                // fall through to default
            }
        }
        return DEFAULT_MAX_ROUNDS;
    }

    /** Test-only override. Pass {@code -1} to clear. */
    public static void setMaxRoundsForTests(int value) {
        testOverride = value;
    }
}