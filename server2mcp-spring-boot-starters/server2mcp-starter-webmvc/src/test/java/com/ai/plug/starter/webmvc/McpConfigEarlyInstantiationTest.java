/**
 * @header-start
 * @module McpConfigEarlyInstantiationTest
 * @keywords 提前实例化 配置类增强 proxyBeanMethods ConfigurationClassPostProcessor
 *           Cannot enhance @Configuration bean definition 启动告警 内部配置类
 *           SpringApplication 日志捕获 McpConfig
 * @updated 2026-10-28 12:50
 * @purpose 锁定「框架内部配置类不得在 Spring 增强阶段被过早实例化」—— 否则每个下游用户启动都看到 4 条 Spring WARN
 * @layer test
 * @see McpConfig — 被约束的自动配置类（位于 server2mcp-autoconfigure）
 * @author han
 * @header-end
 */

package com.ai.plug.starter.webmvc;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 集成测试：框架内部 {@code @Configuration} 类不得在 Spring 的 CGLIB 增强阶段被过早实例化。
 *
 * <p>现象：下游用户应用启动时，Spring 打印 4 条
 * {@code Cannot enhance @Configuration bean definition 'com.ai.plug.autoconfigure.McpConfig$XxxSpecificationConfiguration'
 * since its singleton instance has been created too early}。
 *
 * <p>根因：{@code McpToolScanRegistrar} 在「配置类解析阶段」就 {@code getBean(IToolContext.class)}，
 * 导致承载该 bean 定义的内部 {@code @Configuration} 类被提前实例化；此时 Spring 正准备对它做 CGLIB 增强，
 * 发现单例已在创建中 → 无法增强 → 打印警告并放弃增强。
 *
 * <p>修复：内部配置类一律声明 {@code @Configuration(proxyBeanMethods = false)}。
 * 内部各 {@code @Bean} 方法之间零互调（已核实），关闭代理语义无任何行为影响。
 *
 * <p>说明：本测试锁定的是结构约束（可稳定断言）；「启动日志中不再出现该警告」由
 * {@code scripts/verify-downstream-consumption.sh} 在真实下游工程中做端到端断言
 * （Spring Boot 的日志系统会在 refresh 期间重置手工挂载的 appender，进程内捕获不可靠）。
 */
class McpConfigEarlyInstantiationTest {

    @Test
    void 内部配置类必须声明_proxyBeanMethods_false() throws Exception {
        Class<?> mcpConfig = Class.forName("com.ai.plug.autoconfigure.McpConfig");

        List<Class<?>> configClasses = java.util.Arrays.stream(mcpConfig.getDeclaredClasses())
                .filter(c -> c.getAnnotation(Configuration.class) != null)
                .toList();

        assertThat(configClasses)
                .as("McpConfig 应至少含一个内部 @Configuration 类，否则本测试失去意义")
                .isNotEmpty();

        for (Class<?> configClass : configClasses) {
            Configuration configuration = configClass.getAnnotation(Configuration.class);
            assertThat(configuration.proxyBeanMethods())
                    .as("内部配置类 %s 必须声明 proxyBeanMethods=false —— 否则 Spring 在 CGLIB 增强阶段"
                            + "遇到它被过早实例化时会打印 'Cannot enhance @Configuration bean definition' 警告，"
                            + "污染每个下游用户的启动日志",
                            configClass.getSimpleName())
                    .isFalse();
        }
    }
}
