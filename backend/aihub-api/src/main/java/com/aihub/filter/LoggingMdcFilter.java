package com.aihub.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 日志 MDC 过滤器
 * 设置请求相关的上下文信息到 MDC，供日志系统使用
 */
@Component
@Order(1) // 确保在其他 Filter 之前执行
public class LoggingMdcFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            
            try {
                // 获取请求ID（如果已设置）
                String requestId = (String) httpRequest.getAttribute("__requestId");
                if (requestId == null) {
                    requestId = UUID.randomUUID().toString();
                    httpRequest.setAttribute("__requestId", requestId);
                }
                MDC.put("requestId", requestId);
                
                // 获取IP地址
                String ip = getClientIpAddress(httpRequest);
                MDC.put("ip", ip);
                
                // 用户ID 在 AuthInterceptor 中设置（因为需要从 Token 中解析）
                // 这里先不设置，由拦截器设置
                
            } catch (Exception e) {
                // 设置 MDC 失败不影响请求处理
            }
        }
        
        try {
            chain.doFilter(request, response);
        } finally {
            // 请求结束后清除 MDC
            MDC.clear();
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "未知";
    }
}
