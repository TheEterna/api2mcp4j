package com.ai.plug.core.register.complete;

import com.ai.plug.core.context.complete.ICompleteContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

/**
 * @header-start
 * @module ClassPathCompleteScanner
 * @keywords 补全扫描 ClassPathBeanDefinitionScanner bean命名生成 补全注册时点
 *           ICompleteContext addComplete 补全注册
 * @updated 2026-10-28 12:20
 * @purpose 在 bean 命名生成的那一刻把命中补全注册进 ICompleteContext，实现"扫描即注册"
 * @layer core
 * @affects 改动影响补全注册时点，进而影响 Provider 的补全暴露清单
 * @see McpCompleteScanConfigurer — 构造并驱动本扫描器
 * @see ICompleteContext — 注册发生的容器
 * @constraint 补全注册的唯一切入点
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/6/16 17:08
 */

public class ClassPathCompleteScanner extends ClassPathBeanDefinitionScanner {

    private ICompleteContext completeContext;
    public ClassPathCompleteScanner(BeanDefinitionRegistry registry, ICompleteContext completeContext) {
        super(registry, false);
        setBeanNameGenerator(new ClassPathCompleteScanner.CompleteBeanNameGenerator(completeContext));
    }


    public static class CompleteBeanNameGenerator extends AnnotationBeanNameGenerator {
        private ICompleteContext completeContext;
        public CompleteBeanNameGenerator(ICompleteContext completeContext) {
            this.completeContext = completeContext;
        }

        @Override
        public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
            String beanName = super.generateBeanName(definition, registry);

            completeContext.addComplete(beanName);

            return beanName;
        }
    }}
