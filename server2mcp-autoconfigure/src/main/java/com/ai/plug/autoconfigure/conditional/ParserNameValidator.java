/**
 * @header-start
 * @module ParserNameValidator
 * @keywords 启动断言 解析器名校验 注册纪律 R2 V-2 ApplicationRunner strict
 *           plugin.mcp.parser.des plugin.mcp.parser.param 未知名 拒绝启动 告警
 *           拼写错误 静默不激活 Environment
 * @updated 2026-10-28 13:45
 * @purpose 在容器启动完成时校验已配置的解析器名是否合法，把「拼错即静默失效」变成启动期可见信号
 * @layer autoconfigure
 * @see ParserNames — 已知名集合
 * @see Conditions.ParserCondition — 实际决定解析器激活与否的条件判定
 * @constraint 注册纪律 R2 的落地实现：默认只告警不阻断（向后兼容），
 *                      开启 plugin.mcp.parser.strict=true 时拒绝启动
 * @author han
 * @header-end
 */

package com.ai.plug.autoconfigure.conditional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 启动期解析器名校验器（注册纪律 R2 · 违规清单 V-2 的落地）。
 *
 * <p><b>缺口</b>：{@link Conditions.ParserCondition} 只做「配置里有没有这个名字」的比对。
 * 把 {@code JAVADOC} 拼成 {@code JAVADOCS}，该解析器<b>静默不激活</b>、启动<b>零告警</b>，
 * 要到运行时才发现工具描述缺失或参数 Schema 退化。
 *
 * <p><b>处置分级</b>：
 * <ul>
 *   <li>默认（{@code plugin.mcp.parser.strict=false}）：打印 WARN 并列出未知名与合法名，不阻断启动
 *       —— 保持对既有用户的逐字节兼容</li>
 *   <li>{@code plugin.mcp.parser.strict=true}：抛 {@link IllegalStateException} 拒绝启动，
 *       供 CI 与严格生产环境使用</li>
 * </ul>
 *
 * <p><b>匹配规则</b>：与 {@code Conditions.ParserCondition} 的 {@code equalsIgnoreCase} 保持一致
 * （大小写不敏感），避免出现「校验器说不合法、但条件判定却能命中」的矛盾。
 */
@Slf4j
public class ParserNameValidator implements ApplicationRunner {

    private static final String CONFIG_DES = "plugin.mcp.parser.des";

    private static final String CONFIG_PARAM = "plugin.mcp.parser.param";

    private static final String CONFIG_STRICT = "plugin.mcp.parser.strict";

    private final Environment environment;

    public ParserNameValidator(Environment environment) {
        this.environment = environment;
    }

    /**
     * 收集配置中未知的解析器名。
     *
     * @return 形如 {@code des.JAVADOCS} / {@code param.SPRINGMVCX} 的清单；全部合法时返回空列表
     */
    public List<String> collectUnknownParserNames() {
        List<String> unknown = new ArrayList<>();
        collectUnknown(CONFIG_DES, ParserNames.DES_NAMES, unknown);
        collectUnknown(CONFIG_PARAM, ParserNames.PARAM_NAMES, unknown);
        return unknown;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> unknown = collectUnknownParserNames();
        if (unknown.isEmpty()) {
            return;
        }
        String message = buildMessage(unknown);
        if (isStrict()) {
            throw new IllegalStateException(message);
        }
        log.warn(message);
    }

    private void collectUnknown(String configKey, Set<String> knownNames, List<String> unknown) {
        String[] configured = environment.getProperty(configKey, String[].class, new String[0]);
        String shortKey = configKey.substring(configKey.lastIndexOf('.') + 1);
        for (String name : configured) {
            if (name == null || name.isBlank()) {
                continue;
            }
            String trimmed = name.trim();
            if (!knownNames.contains(trimmed.toUpperCase())) {
                unknown.add(shortKey + "." + trimmed);
            }
        }
    }

    private boolean isStrict() {
        return environment.getProperty(CONFIG_STRICT, Boolean.class, false);
    }

    private String buildMessage(List<String> unknown) {
        StringBuilder message = new StringBuilder()
                .append("plugin.mcp.parser 配置中存在未知的解析器名：")
                .append(String.join("、", unknown))
                .append("。未知的解析器不会激活，且不会有任何其他提示 —— 请核对其拼写。")
                .append("合法名称：des=")
                .append(String.join("/", sorted(ParserNames.DES_NAMES)))
                .append("，param=")
                .append(String.join("/", sorted(ParserNames.PARAM_NAMES)))
                .append("。")
                .append("如需在启动时直接拒绝（拒绝而非告警），请设置 plugin.mcp.parser.strict=true。");
        return message.toString();
    }

    private List<String> sorted(Set<String> names) {
        return names.stream().sorted().toList();
    }
}
