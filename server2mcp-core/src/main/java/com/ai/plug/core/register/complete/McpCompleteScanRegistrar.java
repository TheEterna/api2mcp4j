package com.ai.plug.core.register.complete;

import com.ai.plug.core.annotation.McpCompleteScan;
import com.ai.plug.core.context.complete.ICompleteContext;
import com.ai.plug.core.utils.CustomToolUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @header-start
 * @module McpCompleteScanRegistrar
 * @keywords 补全扫描 注册链路 ImportBeanDefinitionRegistrar BeanFactoryAware McpCompleteScan
 *           McpCompleteScans basePackages includeFilters excludeFilters 注解属性读取
 *           ICompleteContext McpCompleteScanConfigurer Bean定义注册
 * @updated 2026-10-28 12:20
 * @purpose 读取 @McpCompleteScan/@McpCompleteScans 注解属性并注册 McpCompleteScanConfigurer 的 Bean 定义
 * @layer core
 * @affects 改动影响 @McpCompleteScan 全部属性（包范围与过滤器）的读取与向 Configurer 的装配
 * @see McpCompleteScanConfigurer — 被注册的执行体
 * @see ICompleteContext — 补全注册定义的容器
 * @constraint 注册链路四件套之一；属性名与 @McpCompleteScan 注解定义强耦合，须同步修改
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/6/16 17:07
 */

public class McpCompleteScanRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware {
    private static int index = 0;
    private ICompleteContext completeContext;
    private BeanFactory beanFactory;
    public McpCompleteScanRegistrar() {
    }
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取 @McpResourceScan 注解的属性值
        AnnotationAttributes attributes = (AnnotationAttributes) importingClassMetadata.getAnnotationAttributes(McpCompleteScan.class.getName());

        if (attributes != null) {
            registerBeanDefinitions(importingClassMetadata, attributes, registry);
        }
    }
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, AnnotationAttributes attributes, BeanDefinitionRegistry registry) {

        this.completeContext = beanFactory.getBean(ICompleteContext.class);
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(McpCompleteScanConfigurer.class);

        AnnotationAttributes[] basePackages = attributes.getAnnotationArray("basePackages");
        if (!CollectionUtils.isEmpty(List.of(basePackages))) {
            builder.addPropertyValue("basePackages", basePackages);
        } else {
            builder.addPropertyValue("basePackages", getDefaultBasePackage(importingClassMetadata));
        }

        AnnotationAttributes[] excludeFilters = attributes.getAnnotationArray("excludeFilters");
        if (!CollectionUtils.isEmpty(List.of(basePackages))) {
            builder.addPropertyValue("excludeFilters", excludeFilters);
        }

        AnnotationAttributes[] includeFilters = attributes.getAnnotationArray("includeFilters");
        if (!CollectionUtils.isEmpty(List.of(includeFilters))) {
            builder.addPropertyValue("includeFilters", includeFilters);
        }
        builder.addPropertyValue("completeContext", this.completeContext);

        builder.setRole(2);
        registry.registerBeanDefinition(generateBaseBeanName(importingClassMetadata, index++), builder.getBeanDefinition());
    }

    private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata, int index) {
        return CustomToolUtil.generateBaseBeanName(importingClassMetadata, index);
    }
    private static String getDefaultBasePackage(AnnotationMetadata importingClassMetadata) {
        return ClassUtils.getPackageName(importingClassMetadata.getClassName());
    }




    /**
     * 给McpCompleteScans用
     */
    public static class RepeatingRegistrar extends McpCompleteScanRegistrar {

        public RepeatingRegistrar() {
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            AnnotationAttributes attributes = (AnnotationAttributes) importingClassMetadata.getAnnotationAttributes(McpCompleteScan.class.getName());
            AnnotationAttributes[] annotations = attributes.getAnnotationArray("value");
            for (AnnotationAttributes annotation : annotations) {
                this.registerBeanDefinitions(importingClassMetadata, annotation, registry);
            }
        }

    }

}
