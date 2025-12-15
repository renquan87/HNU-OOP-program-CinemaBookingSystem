package com.cinema.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HTTP 响应拦截器
 * 修复 Vite 代理中的分块编码问题，确保正确的响应头
 */
@Component
public class HttpResponseInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 设置通用的响应头
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type, Authorization");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        
        // 禁用 gzip 压缩可能会帮助解决某些代理问题
        response.setHeader("Content-Encoding", "");
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 确保响应体被完全写入
        try {
            response.flushBuffer();
        } catch (Exception e) {
            // 忽略关闭时的异常
        }
    }
}
