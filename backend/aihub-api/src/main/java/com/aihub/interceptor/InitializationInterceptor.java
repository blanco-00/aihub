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
        long startTime = System.currentTimeMillis();
        String requestPath = request.getRequestURI();
        
        // 获取请求到达服务器的时间（从Filter中设置）
        Long requestArrivalTime = (Long) request.getAttribute("__requestArrivalTime");
        String requestId = (String) request.getAttribute("__requestId");
        
        // 只记录性能警告（超过100ms的请求）
        if (requestArrivalTime != null) {
            long timeFromArrival = startTime - requestArrivalTime;
            if (timeFromArrival > 100) {
                log.warn("[性能警告] {} 请求在到达拦截器前耗时过长: {}ms, {}", 
                        requestId, timeFromArrival, requestPath);
            }
        }
        
        // 排除初始化相关的API和页面，允许访问
        if (requestPath.startsWith("/api/init/") || 
            requestPath.startsWith("/api/auth/login") ||
            requestPath.startsWith("/api/auth/refresh") ||
            requestPath.equals("/init") ||
            requestPath.equals("/login") ||
            requestPath.equals("/error")) {
            return true;
        }
        
        // 对于其他请求，检查系统是否已初始化
        long checkStart = System.currentTimeMillis();
        boolean initialized = initializationService.isInitialized();
        long checkTime = System.currentTimeMillis() - checkStart;
        
        if (checkTime > 100) {
            log.warn("[性能警告] 初始化状态检查耗时过长: {}ms", checkTime);
        }
        
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
