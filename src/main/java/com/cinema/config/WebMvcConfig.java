package com.cinema.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Web MVC 配置类
 * 注册HTTP拦截器以修复代理问题
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private HttpResponseInterceptor httpResponseInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpResponseInterceptor)
                .addPathPatterns("/api/**");
    }
}
