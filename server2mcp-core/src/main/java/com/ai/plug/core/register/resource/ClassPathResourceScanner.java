package com.ai.plug.core.register.resource;

import com.ai.plug.core.context.resource.IResourceContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;


/**
 * @header-start
 * @module ClassPathResourceScanner
 * @keywords 资源扫描 ClassPathBeanDefinitionScanner bean命名生成 资源注册时点
 *           IResourceContext addResource 资源注册
 * @updated 2026-10-28 12:20
 * @purpose 在 bean 命名生成的那一刻把命中资源注册进 IResourceContext，实现"扫描即注册"
 * @layer core
 * @affects 改动影响资源注册时点，进而影响 Provider 的资源暴露清单
 * @see McpResourceScanConfigurer — 构造并驱动本扫描器
 * @see IResourceContext — 注册发生的容器
 * @constraint 资源注册的唯一切入点
 * @author han
 * @header-end
 */

/**
 * @author han
 * time: 2025/04/2025/4/10 15:49
 * des:
 */

public class ClassPathResourceScanner extends ClassPathBeanDefinitionScanner {


    public ClassPathResourceScanner(BeanDefinitionRegistry registry,
                                    IResourceContext resourceContext) {
        super(registry, false);
        setBeanNameGenerator(new ResourceBeanNameGenerator(resourceContext));
    }


    public static class ResourceBeanNameGenerator extends AnnotationBeanNameGenerator {
        private final IResourceContext resourceContext;
        public ResourceBeanNameGenerator(IResourceContext resourceContext) {
            this.resourceContext = resourceContext;
        }

        @Override
        public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
            String beanName = super.generateBeanName(definition, registry);

            this.resourceContext.addResource(beanName);

            return beanName;
        }
    }


}
