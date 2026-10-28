package com.ai.plug.core.spec.capabilities;

import io.modelcontextprotocol.spec.McpSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @header-start
 * @module CapabilitiesHealth
 * @keywords 能力健康检查 ServerCapabilities listChanged 必需标志 健康报告 Report
 *           CapabilitySnapshot 协议2026-07-28 运维可观测 校验
 * @updated 2026-10-28 12:20
 * @purpose 校验服务端能力集是否满足协议最小必需标志，产出可读的健康报告
 * @layer core
 * @author han
 * @header-end
 */

/**
 * Capabilities health check — verifies that the configured
 * {@link McpSchema.ServerCapabilities} carry the protocol-2026-07-28
 * minimum viable set of listChanged flags + produce a human-readable
 * health report.
 *
 * <h2>用法</h2>
 * <pre>{@code
 *   CapabilitiesHealth.Report report = CapabilitiesHealth.check(caps);
 *   if (!report.isHealthy()) {
 *       log.warn("Capabilities health issues: {}", report);
 *   }
 * }</pre>
 *
 * <p>Strict by default (e.g. "tools.listChanged must be true"). For a
 * looser check (e.g. for non-listChanged servers), pass a custom
 * required-flags set.
 */
public final class CapabilitiesHealth {

    /** Default required flags per protocol 2026-07-28. */
    public static final Set<String> DEFAULT_REQUIRED_FLAGS = Set.of(
        "tools.listChanged"
    );

    private CapabilitiesHealth() {
    }

    /**
     * Run a health check with the default required-flags set.
     */
    public static Report check(McpSchema.ServerCapabilities caps) {
        return check(caps, DEFAULT_REQUIRED_FLAGS);
    }

    /**
     * Run a health check with a custom required-flags set. Pass {@link Set#of()}
     * for a no-op check (always healthy).
     */
    public static Report check(McpSchema.ServerCapabilities caps, Set<String> required) {
        Report report = new Report();
        CapabilitySnapshot snap = CapabilitySnapshot.from(caps);

        for (String req : required) {
            Boolean value = snap.flags().get(req);
            if (value == null) {
                report.issues.add("missing required flag: " + req);
            }
            else if (!value) {
                report.issues.add("required flag is false: " + req);
            }
        }
        return report;
    }

    /** Health report. */
    public static final class Report {
        private final List<String> issues = new ArrayList<>();

        public boolean isHealthy() {
            return issues.isEmpty();
        }

        public List<String> issues() {
            return List.copyOf(issues);
        }

        @Override
        public String toString() {
            if (isHealthy()) {
                return "CapabilitiesHealth: HEALTHY";
            }
            return "CapabilitiesHealth: UNHEALTHY (" + issues.size() + " issue(s))\n  - "
                + String.join("\n  - ", issues);
        }
    }
}