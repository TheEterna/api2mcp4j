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
import org.springframework.util.ReflectionUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        Set<String> declared = new LinkedHashSet<>();
        ReflectionUtils.doWithMethods(McpConfig.class, method -> {
            ConditionalOnParser annotation = AnnotationUtils.findAnnotation(method, ConditionalOnParser.class);
            if (annotation != null) {
                declared.add(annotation.value());
            }
        });

        assertThat(declared)
                .as("McpConfig 应当声明了若干解析器 Bean")
                .isNotEmpty();
        assertThat(ParserNames.DES_NAMES)
                .as("声明的解析器名未登记进已知名集合 —— 新增解析器时必须同步 ParserNames 与 PluginProperties 枚举")
                .containsAll(declared);
    }

    @Test
    @DisplayName("解析器 Bean 声明数量应与已知名集合规模相符（防漏登记）")
    void declaredParserCountShouldMatchKnownNames() {
        long declaredCount = Arrays.stream(McpConfig.class.getDeclaredMethods())
                .filter(m -> AnnotationUtils.findAnnotation(m, ConditionalOnParser.class) != null)
                .count();

        // des 6 个 + param 7 个 = 13 处声明；已知名集合 des 6 + param 7 = 13
        assertThat(declaredCount).isEqualTo(ParserNames.DES_NAMES.size() + ParserNames.PARAM_NAMES.size());
    }

    @Test
    @DisplayName("已知名集合不得包含重复或空名")
    void knownNamesMustBeClean() {
        for (Set<String> names : List.of(ParserNames.DES_NAMES, ParserNames.PARAM_NAMES)) {
            assertThat(names).doesNotContainNull().doesNotContain("");
        }
    }

    /** 反射工具未覆盖到的兜底：确保 McpConfig 里没有遗漏的非 public 解析器声明方法。 */
    @Test
    void noParserDeclarationHiddenInNestedClasses() {
        long inOuter = Arrays.stream(McpConfig.class.getDeclaredMethods())
                .filter(m -> AnnotationUtils.findAnnotation(m, ConditionalOnParser.class) != null)
                .count();
        long inNested = Arrays.stream(McpConfig.class.getDeclaredClasses())
                .flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                .filter(m -> AnnotationUtils.findAnnotation(m, ConditionalOnParser.class) != null)
                .count();

        assertThat(inOuter + inNested)
                .isEqualTo(ParserNames.DES_NAMES.size() + ParserNames.PARAM_NAMES.size());
    }
}
