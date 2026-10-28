package com.ai.plug.core.provider;

import com.ai.plug.core.annotation.McpResource;
import com.ai.plug.core.spec.callback.resource.AsyncMcpResourceMethodCallback;
import com.ai.plug.core.spec.callback.resource.SyncMcpResourceMethodCallback;
import com.fasterxml.jackson.annotation.*;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.*;
import io.modelcontextprotocol.util.Assert;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * @header-start
 * @module McpResourceProvider
 * @keywords 资源暴露 桥接模式 ResourceSpecification SyncResourceSpecification AsyncResourceSpecification
 *           资源注解 资源回调创建 资源清单
 * @updated 2026-10-28 12:20
 * @purpose 把资源对象转成 MCP SDK 的 ResourceSpecification，是资源注册面到暴露面的桥
 * @layer core
 * @affects 改动影响 MCP 客户端看到的全部资源清单与资源元数据
 * @see IResourceContext — 资源注册定义的容器
 * @constraint 注册纪律「利器 A」：暴露清单须与注册面同源，不得另开数据源
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/6/19 17:48
 */

public class McpResourceProvider {
    private final List<Object> resourceObjects;

    public McpResourceProvider(List<Object> resourceObjects) {
        Assert.notNull(resourceObjects, "resourceObjects cannot be null");
        this.resourceObjects = resourceObjects;
	}

    /**
     * Returns a list of resource specifications for async resource methods.
     * @return List<McpServerFeatures.AsyncResourceSpecification>
     */
    public List<McpServerFeatures.AsyncResourceSpecification> getAsyncResourceSpecifications() {

        List<McpServerFeatures.AsyncResourceSpecification> methodCallbacks = this.resourceObjects.stream()
                .map(resourceObject -> Stream.of(doGetClassMethods(resourceObject))
                        .filter(resourceMethod -> resourceMethod.isAnnotationPresent(McpResource.class))
                        .map(mcpResourceMethod -> {
                            McpResource resourceAnnotation = mcpResourceMethod.getAnnotation(McpResource.class);

                            String uri = resourceAnnotation.uri();
                            String name = getName(mcpResourceMethod, resourceAnnotation);
                            String title = getTitle(mcpResourceMethod, resourceAnnotation);
                            String description = resourceAnnotation.description();
                            String mimeType = resourceAnnotation.mimeType();

                            var mcpResource = new McpSchema.Resource(uri, name, title, description, mimeType,null, null, null);

                            AsyncMcpResourceMethodCallback methodCallback = AsyncMcpResourceMethodCallback.builder()
                                    .method(mcpResourceMethod)
                                    .bean(resourceObject)
                                    .resource(mcpResource)
                                    .build();

                            return new McpServerFeatures.AsyncResourceSpecification(mcpResource, methodCallback);
                        })
                        .toList())
                .flatMap(List::stream)
                .toList();

        return methodCallbacks;
    }

    /**
     * Returns a list of resource specifications for sync resource methods.
     * @return List<McpServerFeatures.SyncResourceSpecification>
     */
    public List<McpServerFeatures.SyncResourceSpecification> getSyncResourceSpecifications() {

        List<McpServerFeatures.SyncResourceSpecification> methodCallbacks = this.resourceObjects.stream()
                .map(resourceObject -> Stream.of(doGetClassMethods(resourceObject))
                        .filter(resourceMethod -> resourceMethod.isAnnotationPresent(McpResource.class))
                        .map(mcpResourceMethod -> {
                            McpResource resourceAnnotation = mcpResourceMethod.getAnnotation(McpResource.class);

                            String uri = resourceAnnotation.uri();
                            String name = getName(mcpResourceMethod, resourceAnnotation);
                            String title = getTitle(mcpResourceMethod, resourceAnnotation);
                            String description = resourceAnnotation.description();
                            String mimeType = resourceAnnotation.mimeType();
                            Long size = resourceAnnotation.size();


                            Resource mcpResource = Resource.builder()
                                    .uri(uri)
                                    .name(name)
                                    .title(title)
                                    .description(description)
                                    .mimeType(mimeType)
                                    .size(size)
                                    .annotations(null)
                                    .build();

                            SyncMcpResourceMethodCallback methodCallback = SyncMcpResourceMethodCallback.builder()
                                    .method(mcpResourceMethod)
                                    .bean(resourceObject)
                                    .resource(mcpResource)
                                    .build();

                            return new McpServerFeatures.SyncResourceSpecification(mcpResource, methodCallback);
                        })
                        .toList())
                .flatMap(List::stream)
                .toList();

        return methodCallbacks;
    }

    /**
     * Returns the methods of the given bean class.
     * @param bean the bean instance
     * @return the methods of the bean class
     */
    protected Method[] doGetClassMethods(Object bean) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        Arrays.sort(methods, Comparator
                .comparing(Method::getName)
                .thenComparing(method -> Arrays.toString(method.getParameterTypes())));
        return methods;
    }

    private static String getName(Method method, McpResource resource) {
        Assert.notNull(method, "method cannot be null");
        if (resource == null || resource.name() == null || resource.name().isEmpty()) {
            return method.getName();
        }
        return resource.name();
	}
    private static String getTitle(Method method, McpResource resource) {
        Assert.notNull(method, "method cannot be null");
        if (resource == null || resource.name() == null || resource.name().isEmpty()) {
            return method.getName();
        }
        return resource.title();
    }


}
