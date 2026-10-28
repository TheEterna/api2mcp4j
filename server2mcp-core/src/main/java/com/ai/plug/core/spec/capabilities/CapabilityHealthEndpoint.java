package com.ai.plug.core.spec.capabilities;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @header-start
 * @module CapabilityHealthEndpoint
 * @keywords 健康端点 HTTP契约 Map响应 CapacitiesHealthReportActuator 运维看板
 *           transport适配 core不含Web依赖
 * @updated 2026-10-28 12:20
 * @purpose 提供 HTTP 风格的健康端点契约，输出可序列化的能力健康 Map
 * @layer core
 * @author han
 * @header-end
 */

/**
 * Minimal HTTP-style endpoint contract for exposing
 * {@link CapabilitiesHealthReport} to ops dashboards / Actuator / REST.
 *
 * <h2>设计意图</h2>
 * Avoids pulling in Spring Web or Spring Boot Actuator as a hard dependency
 * of the core module. Downstream modules (server-boot-actuator) implement
 * an actual Spring {@code HealthIndicator} backed by this contract. Unit
 * tests verify the contract independently of any Spring binding.
 *
 * <h2>用法</h2>
 * <pre>{@code
 *   var endpoint = new CapabilityHealthEndpoint(() -> server.spec().capabilities());
 *   Map&lt;String, Object&gt; response = endpoint.handle();
 *   // response: { "healthy": true, "issueCount": 0, "issues": [] }
 * </pre>
 */
public class CapabilityHealthEndpoint {

    private final CapabilitiesHealthReportActuator actuator;

    public CapabilityHealthEndpoint(CapabilitiesHealthReportActuator actuator) {
        if (actuator == null) {
            throw new IllegalArgumentException("actuator is required");
        }
        this.actuator = actuator;
    }

    /**
     * Produce a JSON-serializable Map of the current health report.
     * Intended for transport adapters (Servlet, WebFlux, gRPC) to wrap.
     */
    public Map<String, Object> handle() {
        CapabilitiesHealthReport report = actuator.currentHealth();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("healthy", report.healthy());
        body.put("issueCount", report.issueCount());
        if (!report.issues().isEmpty()) {
            body.put("issues", report.issues());
        }
        return body;
    }
}