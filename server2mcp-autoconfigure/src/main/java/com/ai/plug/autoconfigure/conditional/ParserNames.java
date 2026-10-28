/**
 * @header-start
 * @module ParserNames
 * @keywords 解析器名 常量 单一权威源 已知名集合 ConditionalOnParser 注册纪律
 *           R2 V-2 启动断言 des param MCPTOOL TOOL JACKSON JAVADOC SPRINGMVC SWAGGER
 * @updated 2026-10-28 13:45
 * @purpose 集中承载全部解析器名的字符串常量与已知名集合，供条件注解引用与启动期校验共用
 * @layer autoconfigure
 * @see PluginProperties.DesParserType — 配置契约侧的权威枚举，本类的镜像由单测锁定一致
 * @see ParserNameValidator — 启动期校验器
 * @constraint 单一权威源：新增解析器必须同步本类、PluginProperties 的枚举与 McpConfig 的声明，
 *                      三者一致性由 ParserNameValidatorTest 断言把关
 * @author han
 * @header-end
 */

package com.ai.plug.autoconfigure.conditional;

import java.util.Set;

/**
 * 解析器名常量与已知名集合。
 *
 * <p><b>为什么需要这一层</b>：{@link ConditionalOnParser} 的 {@code value} 必须是编译期常量，
 * 而 {@link com.ai.plug.autoconfigure.PluginProperties} 里的两个枚举无法直接用作注解值
 * （{@code Enum.name()} 不是编译期常量表达式），因此需要一层字符串常量供注解引用。
 *
 * <p><b>权威源</b>：{@link com.ai.plug.autoconfigure.PluginProperties.DesParserType} /
 * {@link com.ai.plug.autoconfigure.PluginProperties.ParamParserType} 两个枚举 —— 它们本就
 * 配置契约的一部分。本类是它们在注解侧的镜像，一致性由
 * {@code ParserNameValidatorTest} 断言把关，杜绝「新增解析器只改一处」的漏登记。
 */
public final class ParserNames {

    /** 自创 MCP 专属注解解析器。 */
    public static final String MCPTOOL = "MCPTOOL";

    /** Spring AI 原生 Tool 系列注解解析器。 */
    public static final String TOOL = "TOOL";

    /** Jackson 系列注解解析器。 */
    public static final String JACKSON = "JACKSON";

    /** Javadoc 解析器（需源码可读，jar 部署下不可用）。 */
    public static final String JAVADOC = "JAVADOC";

    /** Spring MVC 注解解析器（仅参数链）。 */
    public static final String SPRINGMVC = "SPRINGMVC";

    /** Swagger 2 解析器。 */
    public static final String SWAGGER2 = "SWAGGER2";

    /** Swagger 3 解析器。 */
    public static final String SWAGGER3 = "SWAGGER3";

    /** 工具描述解析链的已知名集合。 */
    public static final Set<String> DES_NAMES = Set.of(MCPTOOL, TOOL, JACKSON, JAVADOC, SWAGGER3, SWAGGER2);

    /** 工具参数解析链的已知名集合。 */
    public static final Set<String> PARAM_NAMES = Set.of(MCPTOOL, TOOL, JACKSON, JAVADOC, SPRINGMVC, SWAGGER3, SWAGGER2);

    private ParserNames() {
    }
}
