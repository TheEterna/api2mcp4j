package com.ai.plug.core.register.tool;

import com.ai.plug.core.context.tool.IToolContext;
import com.ai.plug.core.context.tool.ToolContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;


/**
 * @header-start
 * @module ClassPathToolScanner
 * @keywords 类路径扫描 ClassPathBeanDefinitionScanner ToolBeanNameGenerator bean命名生成
 *           工具注册时点 IToolContext addTool ToolRegisterDefinition 注册定义
 * @updated 2026-10-28 12:20
 * @purpose 在 bean 命名生成的那一刻把命中工具注册进 IToolContext，实现"扫描即注册"
 * @layer core
 * @affects 改动影响工具注册的时点与注册定义的绑定方式，进而影响 Provider 的暴露清单
 * @see McpToolScanConfigurer — 构造并驱动本扫描器
 * @see IToolContext — 注册发生的容器
 * @constraint 工具注册的唯一切入点；改这里等于改"注册面"与"暴露面"的对齐方式
 * @author han
 * @header-end
 */

/**
 * @author: han
 * time: 2025/04/2025/4/10 15:49
 * des:
 */

public class ClassPathToolScanner extends ClassPathBeanDefinitionScanner {

    private final ToolContext.ToolRegisterDefinition toolRegisterDefinition;


    public ClassPathToolScanner(BeanDefinitionRegistry registry,
                                ToolContext.ToolRegisterDefinition toolRegisterDefinition,
                                IToolContext toolContext) {
        super(registry, false);
        this.toolRegisterDefinition = toolRegisterDefinition;
        super.setBeanNameGenerator(new ToolBeanNameGenerator(toolContext));
    }


    public class ToolBeanNameGenerator extends AnnotationBeanNameGenerator {
        private final IToolContext toolContext;
        public ToolBeanNameGenerator(IToolContext toolContext) {
            this.toolContext = toolContext;
        }

        @Override
        public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
            String beanName = super.generateBeanName(definition, registry);

            this.toolContext.addTool(beanName, toolRegisterDefinition);

            return beanName;
        }
    }


}
