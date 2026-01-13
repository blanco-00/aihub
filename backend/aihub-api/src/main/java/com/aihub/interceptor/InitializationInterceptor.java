package com.aihub.interceptor;

import com.aihub.service.InitializationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class InitializationInterceptor implements HandlerInterceptor {
    
    @Autowired
    private InitializationService initializationService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestPath = request.getRequestURI();
        
        log.debug("初始化拦截器处理请求: {}", requestPath);
        
        // 排除初始化相关的API和页面，允许访问
        if (requestPath.startsWith("/api/init/") || 
            requestPath.startsWith("/api/auth/login") ||
            requestPath.startsWith("/api/auth/refresh") ||
            requestPath.equals("/init") ||
            requestPath.equals("/login") ||
            requestPath.equals("/error")) {
            log.debug("请求路径在排除列表中，直接放行: {}", requestPath);
            return true;
        }
        
        // 对于其他请求，检查系统是否已初始化
        // 如果已初始化，直接放行，不再拦截
        boolean initialized = initializationService.isInitialized();
        log.debug("系统初始化状态: {}", initialized);
        
        if (initialized) {
            return true;
        }
        
        // 如果未初始化，拦截请求
        // 如果是API请求，返回JSON响应
        if (requestPath.startsWith("/api/")) {
            log.warn("系统未初始化，拦截API请求: {}", requestPath);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":503,\"message\":\"系统未初始化，请先访问 /init 页面创建超级管理员\"}");
            return false;
        }
        // 如果是页面请求，跳转到初始化页面
        log.warn("系统未初始化，重定向到初始化页面: {}", requestPath);
        response.sendRedirect("/init");
        return false;
    }
}
