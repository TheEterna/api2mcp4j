/**
 * @header-start
 * @module ParserNameValidatorTest
 * @keywords 解析器名校验 启动断言 R2 注册纪律 V-2 拼写错误 静默不激活 strict
 *           MockEnvironment ParserNames DesParserType ParamParserType 一致性
 * @updated 2026-10-28 13:40
 * @purpose 锁定「配置的 parser 名拼错时必须在启动期被识别」这一注册纪律 R2 要求
 * @layer test
 * @see ParserNameValidator — 被测的启动期校验器
 * @see ParserNames — 已知名集合的常量视图
 * @author han
 * @header-end
 */

package com.ai.plug.autoconfigure.conditional;

import com.ai.plug.autoconfigure.McpConfig;
import com.ai.plug.autoconfigure.PluginProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.mock.env.MockEnvironment;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 单测：解析器名拼写错误的启动期识别（注册纪律 R2 · 违规清单 V-2）。
 *
 * <p><b>缺口现状</b>：{@code Conditions.ParserCondition} 只做「配置里有没有这个名字」的比对。
 * 把 {@code JAVADOC} 拼成 {@code JAVADOCS}，该解析器静默不激活、启动零告警，
 * 运行时才发现工具描述缺失。本测试类锁定修复后的行为。
 *
 * <p><b>权威源</b>：{@link PluginProperties.DesParserType} / {@link PluginProperties.ParamParserType}
 * 两个枚举 —— 它们本就是配置契约的一部分（虽然 {@code PluginProperties} 自身从未被
 * {@code @EnableConfigurationProperties} 引用，故其枚举强校验从未生效）。
 * {@link ParserNames} 是给注解引用的字符串常量视图，其与枚举的一致性由本测试锁定。
 */
class ParserNameValidatorTest {

    private static MockEnvironment env(String... properties) {
        MockEnvironment environment = new MockEnvironment();
        for (String property : properties) {
            int idx = property.indexOf('=');
            environment.setProperty(property.substring(0, idx), property.substring(idx + 1));
        }
        return environment;
    }

    @Test
    @DisplayName("des 解析器名拼错时必须被识别")
    void unknownDesParserNameShouldBeDetected() {
        MockEnvironment environment = env("plugin.mcp.parser.des=MCPTOOL,JAVADOCS");

        List<String> unknown = new ParserNameValidator(environment).collectUnknownParserNames();

        assertThat(unknown).containsExactly("des.JAVADOCS");
    }

    @Test
    @DisplayName("param 解析器名拼错时必须被识别")
    void unknownParamParserNameShouldBeDetected() {
        MockEnvironment environment = env("plugin.mcp.parser.param=MCPTOOL,SPRINGMVCX");

        List<String> unknown = new ParserNameValidator(environment).collectUnknownParserNames();

        assertThat(unknown).containsExactly("param.SPRINGMVCX");
    }

    @Test
    @DisplayName("全部合法时不得误报")
    void legalParserNamesShouldNotBeReported() {
        MockEnvironment environment = env(
                "plugin.mcp.parser.des=MCPTOOL,TOOL,JACKSON,JAVADOC,SWAGGER3,SWAGGER2",
                "plugin.mcp.parser.param=MCPTOOL,TOOL,JACKSON,JAVADOC,SPRINGMVC,SWAGGER3,SWAGGER2");

        assertThat(new ParserNameValidator(environment).collectUnknownParserNames()).isEmpty();
    }

    @Test
    @DisplayName("未配置解析器列表时不得误报")
    void absentConfigurationShouldNotBeReported() {
        assertThat(new ParserNameValidator(env()).collectUnknownParserNames()).isEmpty();
    }

    @Test
    @DisplayName("大小写不敏感须与 ParserCondition 的 equalsIgnoreCase 保持一致")
    void parserNameMatchingShouldBeCaseInsensitive() {
        MockEnvironment environment = env("plugin.mcp.parser.des=mcpTool,javadoc");

        assertThat(new ParserNameValidator(environment).collectUnknownParserNames()).isEmpty();
    }

    @Test
    @DisplayName("strict=true 时拼错必须拒绝启动")
    void strictModeShouldRejectStartup() {
        MockEnvironment environment = env(
                "plugin.mcp.parser.des=MCPTOOL,JAVADOCS",
                "plugin.mcp.parser.strict=true");

        ParserNameValidator validator = new ParserNameValidator(environment);

        assertThatThrownBy(() -> validator.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JAVADOCS");
    }

    @Test
    @DisplayName("strict=false（默认）时拼错只告警不阻断")
    void nonStrictModeShouldOnlyWarn() {
        MockEnvironment environment = env("plugin.mcp.parser.des=MCPTOOL,JAVADOCS");

        ParserNameValidator validator = new ParserNameValidator(environment);

        assertThatCode(() -> validator.run(null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("已知名集合必须与 PluginProperties 的解析器枚举完全一致")
    void knownNamesMustMatchParserEnums() {
        Set<String> enumDesNames = Arrays.stream(PluginProperties.DesParserType.values())
                .map(Enum::name)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> enumParamNames = Arrays.stream(PluginProperties.ParamParserType.values())
                .map(Enum::name)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        assertThat(ParserNames.DES_NAMES)
                .as("ParserNames.DES_NAMES 必须与 PluginProperties.DesParserType 枚举名集合完全一致")
                .containsExactlyInAnyOrderElementsOf(enumDesNames);
        assertThat(ParserNames.PARAM_NAMES)
                .as("ParserNames.PARAM_NAMES 必须与 PluginProperties.ParamParserType 枚举名集合完全一致")
                .containsExactlyInAnyOrderElementsOf(enumParamNames);
    }

    @Test
    @DisplayName("McpConfig 中每个 @ConditionalOnParser 声明的名字都必须在已知名集合内")
    void everyDeclaredParserMustBeRegistered() {
        Set<String> declared = new LinkedHashSet<>(collectDeclaredParserNames());

        assertThat(declared)
                .as("McpConfig 应当声明了若干解析器 Bean")
                .isNotEmpty();

        Set<String> known = new LinkedHashSet<>(ParserNames.DES_NAMES);
        known.addAll(ParserNames.PARAM_NAMES);
        assertThat(known)
                .as("声明的解析器名未登记进已知名集合 —— 新增解析器时必须同步 ParserNames 与 PluginProperties 枚举")
                .containsAll(declared);
    }

    @Test
    @DisplayName("已知名集合不得包含重复或空名")
    void knownNamesMustBeClean() {
        for (Set<String> names : List.of(ParserNames.DES_NAMES, ParserNames.PARAM_NAMES)) {
            assertThat(names).doesNotContainNull().doesNotContain("");
        }
    }

    /**
     * 解析器声明处数应与已知名集合规模相符（防漏登记）。
     *
     * <p>注意：解析器 Bean 声明位于 {@code McpConfig} 的【内部嵌套配置类】中
     * （Tool / Resource / Prompt / Complete 的 Specification 配置），
     * 因此必须同时扫描外层与全部嵌套类，只扫外层会得到 0。
     *
     * <p>另一注意：同名解析器会跨链重复声明（如 MCPTOOL 在 des 与 param 各一处），
     * 故「声明处数」与「唯一名数」不同 —— 前者 13（des 6 + param 7），后者 7。
     */
    @Test
    void declaredParserCountShouldMatchKnownNames() {
        List<String> declarations = collectDeclaredParserNames();

        // des 6 处 + param 7 处 = 13 处声明；已知名集合 des 6 + param 7 = 13
        assertThat(declarations).hasSize(ParserNames.DES_NAMES.size() + ParserNames.PARAM_NAMES.size());

        // 唯一名集合必须恰等于已知名集合的并集（既不漏登记，也不多登记）
        Set<String> unique = new LinkedHashSet<>(declarations);
        Set<String> known = new LinkedHashSet<>(ParserNames.DES_NAMES);
        known.addAll(ParserNames.PARAM_NAMES);
        assertThat(unique).hasSize(known.size());
    }

    /** 收集 McpConfig 外层与全部内部嵌套类中声明的解析器名（保留重复，按声明处计）。 */
    private static List<String> collectDeclaredParserNames() {
        return Stream.concat(
                        Stream.of(McpConfig.class),
                        Arrays.stream(McpConfig.class.getDeclaredClasses()))
                .flatMap(type -> Arrays.stream(type.getDeclaredMethods()))
                .map(method -> AnnotationUtils.findAnnotation(method, ConditionalOnParser.class))
                .filter(Objects::nonNull)
                .map(ConditionalOnParser::value)
                .toList();
    }
}
