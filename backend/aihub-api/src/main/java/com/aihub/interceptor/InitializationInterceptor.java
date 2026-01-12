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
        
        // 如果未初始化，跳转到初始化页面
        if (!initializationService.isInitialized()) {
            // 如果是API请求，返回JSON响应
            if (requestPath.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":503,\"message\":\"系统未初始化，请先访问 /init 页面创建超级管理员\"}");
                return false;
            }
            // 如果是页面请求，跳转到初始化页面
            response.sendRedirect("/init");
            return false;
        }
        
        return true;
    }
}
