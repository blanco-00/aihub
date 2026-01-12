package com.aihub.config;

import com.aihub.interceptor.InitializationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private InitializationInterceptor initializationInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(initializationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/init/**",
                        "/api/setup/**",  // 排除配置相关 API，允许在未配置数据库时访问
                        "/init",
                        "/setup",  // 排除配置页面
                        "/static/**",
                        "/assets/**",
                        "/error"
                );
    }
}
