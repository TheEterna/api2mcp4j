package com.ai.plug.core.register.prompt;

import com.ai.plug.core.context.prompt.IPromptContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;


/**
 * @header-start
 * @module ClassPathPromptScanner
 * @keywords 提示词扫描 ClassPathBeanDefinitionScanner bean命名生成 提示词注册时点
 *           IPromptContext addPrompt 提示词注册
 * @updated 2026-10-28 12:20
 * @purpose 在 bean 命名生成的那一刻把命中提示词注册进 IPromptContext，实现"扫描即注册"
 * @layer core
 * @affects 改动影响提示词注册时点，进而影响 Provider 的提示词暴露清单
 * @see McpPromptScanConfigurer — 构造并驱动本扫描器
 * @see IPromptContext — 注册发生的容器
 * @constraint 提示词注册的唯一切入点
 * @author han
 * @header-end
 */

/**
 * @author han
 * time: 2025/6/13 11:02
 */

public class ClassPathPromptScanner extends ClassPathBeanDefinitionScanner {


    public ClassPathPromptScanner(BeanDefinitionRegistry registry,
                                  IPromptContext promptContext) {
        super(registry, false);
        setBeanNameGenerator(new PromptBeanNameGenerator(promptContext));
    }


    public static class PromptBeanNameGenerator extends AnnotationBeanNameGenerator {
        private final IPromptContext promptContext;
        public PromptBeanNameGenerator(IPromptContext promptContext) {
            this.promptContext = promptContext;
        }

        @Override
        public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
            String beanName = super.generateBeanName(definition, registry);

            this.promptContext.addPrompt(beanName);

            return beanName;
        }
    }


}
