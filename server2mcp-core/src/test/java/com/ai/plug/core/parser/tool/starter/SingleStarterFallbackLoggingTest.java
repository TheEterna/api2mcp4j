/**
 * @header-start
 * @module SingleStarterFallbackLoggingTest
 * @keywords 兜底解析 日志级别 WARN SingleStarter 默认解析 降噪 描述解析
 *           参数必填性 参数描述 ListAppender 断言 无噪音
 * @updated 2026-10-28 12:50
 * @purpose 锁定「解析链全部落空 → 走默认兜底」这条常规路径不应产生 WARN 日志
 * @layer test
 * @see SingleStarter — 被测的解析链编排器
 * @author han
 * @header-end
 */

package com.ai.plug.core.parser.tool.starter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ai.plug.core.parser.tool.des.AbstractDesParser;
import com.ai.plug.core.parser.tool.param.AbstractParamParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 单测：解析链全部落空时走默认兜底，属常规路径，不应刷 WARN。
 *
 * <p>背景：下游用户应用启动时，每个工具方法都会看到一条
 * "Please note that the parsing module you specified resulted in ... not parsing the param required"
 * 的 WARN。该消息暗示「你配错了解析器」，但实际语义是「所有解析器都没给出答案，框架按设计回退到默认值」——
 * 措辞与级别双重误导，把框架的常规兜底暴露成了用户需要处理的告警。
 */
class SingleStarterFallbackLoggingTest {

    private static final Method TOOL_METHOD;

    static {
        try {
            TOOL_METHOD = SampleTool.class.getMethod("queryOrder", String.class);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Logger starterLogger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        starterLogger = (Logger) LoggerFactory.getLogger(SingleStarter.class);
        appender = new ListAppender<>();
        appender.start();
        starterLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        starterLogger.detachAppender(appender);
        appender.stop();
    }

    @Test
    @DisplayName("描述解析全部落空走默认兜底时，不产生 WARN")
    void describeFallbackShouldNotWarn() {
        SingleStarter starter = new SingleStarter();

        String result = starter.runDesParse(List.of(new NullDesParser()), TOOL_METHOD, SampleTool.class);

        // 兜底结果本身必须正确：默认按驼峰拆分方法名
        assertThat(result).isEqualTo("query order");
        assertThat(warnMessages()).isEmpty();
    }

    @Test
    @DisplayName("参数必填性全部落空走默认兜底时，不产生 WARN")
    void requiredFallbackShouldNotWarn() {
        SingleStarter starter = new SingleStarter();

        Boolean result = starter.runParamRequiredParse(List.of(new NullParamParser()), TOOL_METHOD, SampleTool.class, 0);

        // 兜底结果本身必须正确：默认必填
        assertThat(result).isTrue();
        assertThat(warnMessages()).isEmpty();
    }

    @Test
    @DisplayName("参数描述全部落空走默认兜底时，不产生 WARN")
    void paramDesFallbackShouldNotWarn() {
        SingleStarter starter = new SingleStarter();

        String result = starter.runParamDesParse(List.of(new NullParamParser()), TOOL_METHOD, SampleTool.class, 0);

        assertThat(result).isNotBlank();
        assertThat(warnMessages()).isEmpty();
    }

    private List<String> warnMessages() {
        return appender.list.stream()
                .filter(e -> e.getLevel() == Level.WARN)
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }

    /** 始终返回 null 的描述解析器 —— 模拟「所有描述解析器都没命中」。 */
    private static final class NullDesParser extends AbstractDesParser {
        @Override
        public String doDesParse(Method method, Class<?> toolClass) {
            return null;
        }
    }

    /** 始终返回 null 的参数解析器 —— 模拟「所有参数解析器都没命中」。 */
    private static final class NullParamParser extends AbstractParamParser {
        @Override
        public Boolean doParamRequiredParse(Method toolMethod, Class<?> toolClass, int index) {
            return null;
        }

        @Override
        public String doParamDesParse(Method toolMethod, Class<?> toolClass, int index) {
            return null;
        }
    }

    /** 被解析的工具类。 */
    public static class SampleTool {
        public String queryOrder(String orderId) {
            return "ok";
        }
    }
}
