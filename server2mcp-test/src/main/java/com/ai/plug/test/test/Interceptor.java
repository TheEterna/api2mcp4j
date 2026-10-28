package com.ai.plug.test.test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @header-start
 * @module Interceptor
 * @keywords 拦截器 HandlerInterceptor preHandle 请求日志 请求头打印 客户端IP
 *           调试辅助
 * @updated 2026-10-28 12:20
 * @purpose 演示用拦截器，打印每次请求的 URL、方法、客户端 IP 与请求头
 * @layer test
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * time: 2025/4/23 4:15
 */
@Component
@Slf4j
public class Interceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 打印请求的基本信息
        log.info("请求基本信息：URL: {}, 请求方法: {}, 客户端 IP: {}", request.getRequestURL(), request.getMethod(), request.getRemoteAddr());
        log.info("请求头信息:");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = request.getHeader(headerName);
            log.info("{}: {}", headerName, headerValue);
        });

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
