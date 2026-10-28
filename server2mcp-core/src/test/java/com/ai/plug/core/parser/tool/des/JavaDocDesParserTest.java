/**
 * @header-start
 * @module JavaDocDesParserTest
 * @keywords Javadoc解析器 源码可读性 判定 jar部署 零开销短路 废弃解析器
 *           classloader资源查找 隔离 ClassLoader
 * @updated 2026-10-28 14:00
 * @purpose 锁定「源码不可读时零开销短路」——已废弃的 Javadoc 解析器不应在 jar 部署下白跑 classloader 查找
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 单测：Javadoc 解析器的源码可读性判定与零开销短路。
 *
 * <p><b>背景</b>：{@link JavaDocDesParser} 已标记 {@code @Deprecated}（作者原话：
 * 「javaParser 只能针对 java 源文件，jar 包启动时没有 java 文件，会直接导致报错」）。
 * 但它仍在默认描述解析链里 —— 于是每个下游用户的每个工具方法，都会白跑一次
 * {@code classLoader.getResourceAsStream("xxx/YourController.java")}（在 Spring Boot
 * fat-jar 的嵌套 jar 结构下这一步并不廉价），必然失败，必然返回 null。
 *
 * <p><b>本测试锁定</b>：源码不可读时直接短路，不再触发 classloader 资源查找。
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
    @DisplayName("源码不可读时不得再触发 classloader 资源查找")
    void unreadableSourceMustNotTouchClassLoader() {
        Class<?> mocked = mock(Class.class);
        when(mocked.getProtectionDomain()).thenReturn(null);

        String result = new JavaDocDesParser().doDesParse(mockedMethod(), mocked);

        assertThat(result).isNull();
        verify(mocked, never()).getClassLoader();
    }

    @Test
    @DisplayName("源码不可读时不得抛异常")
    void unreadableSourceMustNotThrow() {
        Class<?> mocked = mock(Class.class);
        when(mocked.getProtectionDomain()).thenReturn(null);

        assertThatCode(() -> new JavaDocDesParser().doDesParse(mockedMethod(), mocked))
                .doesNotThrowAnyException();
    }

    /** 供解析器反射查询的样例方法。 */
    public void sampleMethod() {
    }

    private Method mockedMethod() {
        try {
            return JavaDocDesParserTest.class.getMethod("sampleMethod");
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
