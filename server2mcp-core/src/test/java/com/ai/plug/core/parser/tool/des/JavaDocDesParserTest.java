/**
 * @header-start
 * @module JavaDocDesParserTest
 * @keywords Javadoc解析器 源码可读性 判定 jar部署 零开销短路 废弃解析器
 *           开发环境回归保护 静态方法反射
 * @updated 2026-10-28 14:00
 * @purpose 锁定「源码不可读时零开销短路，且开发环境的 Javadoc 解析能力不受影响」
 * @layer test
 * @see JavaDocDesParser — 被测的（已 @Deprecated 的）描述解析器
 * @author han
 * @header-end
 */

package com.ai.plug.core.parser.tool.des;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 单测：Javadoc 解析器的源码可读性判定与零开销短路。
 *
 * <p><b>背景</b>：{@link JavaDocDesParser} 已标记 {@code @Deprecated}（作者原话：
 * 「javaParser 只能针对 java 源文件，jar 包启动时没有 java 文件，会直接导致报错」）。
 * 但它仍在默认描述解析链里 —— 于是每个下游用户的每个工具方法，都会白跑一次
 * {@code classLoader.getResourceAsStream("xxx/YourController.java")}（在 Spring Boot
 * fat-jar 的嵌套 jar 结构下这一步并不廉价），必然失败，必然返回 null。
 *
 * <p><b>本测试锁定两条行为契约</b>：
 * <ol>
 *   <li>源码不可读（jar 部署）时短路返回 null，不抛异常、不再白跑资源查找</li>
 *   <li>源码可读（开发环境）时 <b>Javadoc 解析能力不受影响</b> —— 这是短路改动的主要回归风险</li>
 * </ol>
 */
class JavaDocDesParserTest {

    @Test
    @DisplayName("从 jar 加载的类应判定为源码不可读")
    void jarLoadedClassShouldBeSourceUnreadable() {
        // McpSchema 来自 mcp jar（依赖 jar），必然不在源码目录中
        assertThat(JavaDocDesParser.isSourceReadable(McpSchema.class)).isFalse();
    }

    @Test
    @DisplayName("从目录加载的类应判定为源码可读")
    void directoryLoadedClassShouldBeSourceReadable() {
        // 本类自身编译产物在 target/classes 下，属开发环境形态
        assertThat(JavaDocDesParser.isSourceReadable(JavaDocDesParser.class)).isTrue();
    }

    @Test
    @DisplayName("源码不可读时应短路返回 null 而不抛异常")
    void unreadableSourceMustReturnNullWithoutThrowing() {
        assertThatCode(() -> {
            String result = new JavaDocDesParser().doDesParse(sampleMethodRef(), McpSchema.class);
            assertThat(result).isNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("开发环境下 Javadoc 解析能力不得受影响")
    void developmentEnvironmentMustStillParseJavadoc() throws Exception {
        // 取一个在源码文件里带 Javadoc 的方法，验证短路改动没有误伤开发环境
        Method method = JavaDocDesParser.class.getDeclaredMethod("getJavaSourceFile", Class.class);

        String description = new JavaDocDesParser().doDesParse(method, JavaDocDesParser.class);

        assertThat(description)
                .as("开发环境（源码在 target/classes 对应的 src/main/java 下）应仍能解析出 Javadoc")
                .isNotNull()
                .contains("源码目录");
    }

    /** 供解析器反射查询的样例方法。 */
    public void sampleMethod() {
    }

    private Method sampleMethodRef() {
        try {
            return JavaDocDesParserTest.class.getMethod("sampleMethod");
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
