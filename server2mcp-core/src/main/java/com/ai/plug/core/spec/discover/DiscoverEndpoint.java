package com.ai.plug.core.spec.discover;

import com.ai.plug.common.utils.JsonParser;
import com.ai.plug.core.spec.implementation.ServerInfoFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @header-start
 * @module DiscoverEndpoint
 * @keywords 服务发现端点 server/discover 协议版本声明 能力声明 serverInfo
 *           CapabilitiesJsonExporter wire形状 版本协商 HTTP层暴露
 * @updated 2026-10-28 12:20
 * @purpose 定义 server/discover 的 HTTP 层端点契约，输出协议版本、服务身份与能力声明
 * @layer core
 * @author han
 * @header-end
 */

/**
 * Framework-layer HTTP endpoint contract for {@code server/discover}.
 *
 * <p>SDK 2.0 has no JSON-RPC schema for {@code server/discover}, so this
 * endpoint is exposed at the HTTP layer (e.g. {@code GET /mcp/discover})
 * rather than over JSON-RPC. Downstream {@code server-boot-actuator} or
 * any HTTP transport can mount this contract directly.
 *
 * <h2>用法</h2>
 * <pre>{@code
 *   DiscoverEndpoint endpoint = new DiscoverEndpoint(
 *       "my-server", "1.0.0",
 *       () -> serverCapabilities,
 *       () -> extensions);
 *   String json = endpoint.handleJson();
 *   // {"protocolVersions":["2026-07-28"],"preferredVersion":"2026-07-28",
 *   //  "serverInfo":{"name":"my-server","version":"1.0.0"},...}
 * }</pre>
 *
 * @author han
 * @time 2026/8/3
 */
public final class DiscoverEndpoint {

    /** Protocol versions this server is willing to speak, in preference order. */
    public static final List<String> SUPPORTED_PROTOCOL_VERSIONS =
        List.of("2026-07-28", "2025-11-25");

    /** The version we'll negotiate to by default. */
    public static final String PREFERRED_PROTOCOL_VERSION = "2026-07-28";

    private final String serverName;
    private final String serverVersion;
    private final java.util.function.Supplier<Object> capabilitiesSupplier;
    private final java.util.function.Supplier<Map<String, Object>> extensionsSupplier;

    public DiscoverEndpoint(String serverName,
                             String serverVersion,
                             java.util.function.Supplier<Object> capabilitiesSupplier,
                             java.util.function.Supplier<Map<String, Object>> extensionsSupplier) {
        if (serverName == null || serverName.isBlank()) {
            throw new IllegalArgumentException("serverName is required");
        }
        if (serverVersion == null || serverVersion.isBlank()) {
            throw new IllegalArgumentException("serverVersion is required");
        }
        if (capabilitiesSupplier == null) {
            throw new IllegalArgumentException("capabilitiesSupplier is required");
        }
        if (extensionsSupplier == null) {
            throw new IllegalArgumentException("extensionsSupplier is required");
        }
        this.serverName = serverName;
        this.serverVersion = serverVersion;
        this.capabilitiesSupplier = capabilitiesSupplier;
        this.extensionsSupplier = extensionsSupplier;
    }

    /**
     * Convenience overload — no extensions.
     */
    public DiscoverEndpoint(String serverName,
                             String serverVersion,
                             java.util.function.Supplier<Object> capabilitiesSupplier) {
        this(serverName, serverVersion, capabilitiesSupplier, Map::of);
    }

    /**
     * Build the discover response as a plain {@link Map}, suitable for
     * any transport that serializes its own map (Jackson, JSON-B, etc.).
     */
    public Map<String, Object> handle() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("protocolVersions", SUPPORTED_PROTOCOL_VERSIONS);
        body.put("preferredVersion", PREFERRED_PROTOCOL_VERSION);
        body.put("serverInfo", ServerInfoFactory.create(serverName, serverVersion));
        body.put("capabilities", capabilitiesSupplier.get());
        Map<String, Object> ext = extensionsSupplier.get();
        if (ext != null && !ext.isEmpty()) {
            body.put("extensions", ext);
        }
        return body;
    }

    /**
     * Build the discover response with {@code capabilities} rendered via
     * the framework's {@link com.ai.plug.core.spec.capabilities.CapabilitiesJsonExporter}
     * — emitting the protocol-2026-07-28 {@code ServerCapabilities} wire
     * schema (including {@code tools.subscription},
     * {@code completions.listChanged}, and the
     * {@code experimental.io.modelcontextprotocol/tasks} extension)
     * regardless of what the upstream supplier returns.
     *
     * <p>If the supplier hands us a {@link com.ai.plug.core.spec.capabilities.WireServerCapabilities}
     * we serialise it directly; otherwise we fall back to the
     * legacy {@link #handle()} path so existing integrators keep working.
     */
    public Map<String, Object> handle2026() {
        Object supplied = capabilitiesSupplier.get();
        if (supplied instanceof com.ai.plug.core.spec.capabilities.WireServerCapabilities wire) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("protocolVersions", SUPPORTED_PROTOCOL_VERSIONS);
            body.put("preferredVersion", PREFERRED_PROTOCOL_VERSION);
            body.put("serverInfo", ServerInfoFactory.create(serverName, serverVersion));
            com.ai.plug.core.spec.capabilities.CapabilitiesJsonExporter exporter =
                new com.ai.plug.core.spec.capabilities.CapabilitiesJsonExporter();
            body.put("capabilities", exporter.toMap(wire));
            Map<String, Object> ext = extensionsSupplier.get();
            if (ext != null && !ext.isEmpty()) {
                body.put("extensions", ext);
            }
            return body;
        }
        return handle();
    }

    /** Convenience: serialize {@link #handle()} as a JSON string using
     *  the project's shared Jackson 3 mapper. */
    public String handleJson() throws java.io.IOException {
        return JsonParser.getObjectMapper().writeValueAsString(handle());
    }

    /** Negotiate the protocol version for a given client preferred version.
     *  Returns the client's preferred version if supported, otherwise the
     *  server's preferred version. */
    public static String negotiate(String clientPreferred) {
        if (clientPreferred == null || clientPreferred.isBlank()) {
            return PREFERRED_PROTOCOL_VERSION;
        }
        String result = VersionNegotiator.negotiate(clientPreferred, SUPPORTED_PROTOCOL_VERSIONS);
        return result != null ? result : PREFERRED_PROTOCOL_VERSION;
    }
}