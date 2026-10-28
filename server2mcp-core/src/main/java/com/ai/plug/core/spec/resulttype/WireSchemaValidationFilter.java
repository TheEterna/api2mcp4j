package com.ai.plug.core.spec.resulttype;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.annotation.Nullable;

/**
 * @header-start
 * @module WireSchemaValidationFilter
 * @keywords 校验过滤器 传输无关 严格模式 宽松告警 Builder
 *             WireSchemaValidator 结果检查 契约
 * @updated 2026-10-28 12:20
 * @purpose 以传输无关的过滤器校验出站结果的线格式元数据，可选严格抛错或宽松告警
 * @layer core
 * @author han
 * @header-end
 */

/**
 * Lightweight, framework-agnostic request/response filter that validates
 * wire-layer metadata on outgoing {@link McpSchema.CallToolResult} payloads.
 *
 * <h2>设计意图</h2>
 * This is a small Java interface — not a Servlet / WebFlux / Spring filter —
 * so the framework can stay transport-agnostic. Each transport (Servlet,
 * WebFlux, custom RPC bridge) implements this filter and decides how to
 * enforce the verdict (e.g. Servlet 500, log + return null, drop).
 *
 * <h2>用法</h2>
 * <pre>{@code
 *   WireSchemaValidationFilter filter = WireSchemaValidationFilter.builder()
 *       .strict(true)                          // throw on issues
 *       .validator(WireSchemaValidator::validate)
 *       .build();
 *   McpSchema.CallToolResult result = ...;
 *   McpSchema.CallToolResult checked = filter.check(result);
 * </pre>
 */
public class WireSchemaValidationFilter {

    private static final Logger log = LoggerFactory.getLogger(WireSchemaValidationFilter.class);

    private final boolean strict;
    @Nullable
    private final java.util.function.Function<McpSchema.CallToolResult, WireSchemaValidator.Report> validator;

    private WireSchemaValidationFilter(boolean strict,
            @Nullable java.util.function.Function<McpSchema.CallToolResult,
                    WireSchemaValidator.Report> validator) {
        this.strict = strict;
        this.validator = validator;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Check the result. Returns the same instance if it passes; throws
     * {@link WireSchemaValidationException} in strict mode on issues, or
     * logs a warning in non-strict mode and returns the result.
     */
    public McpSchema.CallToolResult check(McpSchema.CallToolResult result) {
        if (result == null) {
            return null;
        }
        if (validator == null) {
            return result; // no validator configured = pass-through
        }
        WireSchemaValidator.Report report = validator.apply(result);
        if (report.isOk()) {
            return result;
        }
        if (strict) {
            throw new WireSchemaValidationException(result, report);
        }
        log.warn("WireSchema validation found issues (non-strict):\n{}", report);
        return result;
    }

    public boolean isStrict() { return strict; }

    /** Exception thrown by strict mode. Carries the offending result + report. */
    public static class WireSchemaValidationException extends RuntimeException {
        private final McpSchema.CallToolResult result;
        private final WireSchemaValidator.Report report;

        public WireSchemaValidationException(McpSchema.CallToolResult result,
                                           WireSchemaValidator.Report report) {
            super("WireSchema validation failed:\n" + report);
            this.result = result;
            this.report = report;
        }

        public McpSchema.CallToolResult result() { return result; }
        public WireSchemaValidator.Report report() { return report; }
    }

    /** Builder. */
    public static final class Builder {
        private boolean strict = false;
        @Nullable
        private java.util.function.Function<McpSchema.CallToolResult, WireSchemaValidator.Report> validator;

        public Builder strict(boolean strict) {
            this.strict = strict;
            return this;
        }

        public Builder validator(
                java.util.function.Function<McpSchema.CallToolResult, WireSchemaValidator.Report> v) {
            this.validator = v;
            return this;
        }

        public WireSchemaValidationFilter build() {
            return new WireSchemaValidationFilter(strict, validator);
        }
    }
}