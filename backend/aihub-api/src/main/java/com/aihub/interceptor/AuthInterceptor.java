package com.aihub.interceptor;

import com.aihub.dto.Result;
import com.aihub.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

/**
 * 认证拦截器
 * 用于验证JWT Token和权限控制
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 不需要认证的路径
     */
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/refresh",
            "/api/init/",
            "/error"
    );
    
    /**
     * 需要超级管理员权限的路径
     */
    private static final List<String> SUPER_ADMIN_PATHS = Arrays.asList(
            "/api/users"
    );
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestPath = request.getRequestURI();
        
        // 检查是否在排除列表中
        for (String excludePath : EXCLUDE_PATHS) {
            if (requestPath.startsWith(excludePath)) {
                return true;
            }
        }
        
        // 获取Token
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供认证Token");
            return false;
        }
        
        String token = authorization.substring(7);
        
        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token无效或已过期");
            return false;
        }
        
        // 将用户信息存储到request中，供后续使用
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        
        return true;
    }
    
    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        
        Result<Object> result = Result.error(message);
        String json = objectMapper.writeValueAsString(result);
        response.getWriter().write(json);
    }
}
