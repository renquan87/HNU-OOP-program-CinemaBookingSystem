package com.cinema.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * HTTP 响应配置类
 * 修复 Vite 代理中的 "Expected LF after chunk data" 错误
 * 原因：确保响应头正确设置，避免分块传输编码问题
 */
@Configuration
public class HttpResponseConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("json", MediaType.APPLICATION_JSON)
                .favorPathExtension(false)
                .favorParameter(false)
                .ignoreAcceptHeader(false)
                .useRegisteredExtensionsOnly(true);
    }
}
