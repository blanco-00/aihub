package com.aihub.config;

import com.aihub.interceptor.AuthInterceptor;
import com.aihub.interceptor.InitializationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private InitializationInterceptor initializationInterceptor;
    
    @Autowired
    private AuthInterceptor authInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 初始化拦截器（优先级高，先执行）
        registry.addInterceptor(initializationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/init/**",
                        "/api/auth/login",  // 排除登录接口
                        "/api/auth/refresh",  // 排除刷新Token接口
                        "/init",
                        "/login",  // 排除登录页面
                        "/static/**",
                        "/assets/**",
                        "/error"
                )
                .order(1);
        
        // 认证拦截器（优先级低，后执行）
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/auth/logout",  // 登出接口需要认证，但由拦截器内部处理
                        "/api/init/**"
                )
                .order(2);
    }
}
