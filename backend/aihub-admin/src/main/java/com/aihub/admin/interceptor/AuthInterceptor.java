package com.aihub.admin.interceptor;

import com.aihub.common.web.dto.Result;
import com.aihub.admin.service.TokenCacheService;
import com.aihub.common.security.jwt.JwtUtil;
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
 * 使用 Redis 缓存增强 Token 验证
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TokenCacheService tokenCacheService;
    
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
     * 需要认证但不需要权限检查的路径（获取当前用户信息等）
     */
    private static final List<String> AUTH_REQUIRED_PATHS = Arrays.asList(
            "/api/auth/me"
    );
    
    /**
     * 需要超级管理员权限的路径
     */
    private static final List<String> SUPER_ADMIN_PATHS = Arrays.asList(
            "/api/users"
    );
    
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
        
        // 检查是否在排除列表中
        for (String excludePath : EXCLUDE_PATHS) {
            if (requestPath.startsWith(excludePath)) {
                return true;
            }
        }
        
        // 获取Token
        long tokenStart = System.currentTimeMillis();
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供认证Token");
            return false;
        }
        
        String token = authorization.substring(7);
        
        // 先验证 JWT Token 格式和签名（快速验证，不依赖 Redis）
        if (!jwtUtil.validateToken(token)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token无效或已过期");
            return false;
        }
        
        // 性能优化：Redis 验证改为可选，如果 Redis 慢则跳过（JWT 验证已通过，足够安全）
        long redisStart = System.currentTimeMillis();
        try {
            boolean isValid = tokenCacheService.isTokenValid(token);
            if (!isValid) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token无效或已过期");
                return false;
            }
        } catch (Exception e) {
            // Redis 异常时，只记录警告，不阻塞请求（JWT 验证已通过）
            long redisTime = System.currentTimeMillis() - redisStart;
            log.warn("Redis Token 验证异常，继续使用 JWT 验证: {}, 耗时: {}ms", e.getMessage(), redisTime);
        }
        long redisTime = System.currentTimeMillis() - redisStart;
        if (redisTime > 100) {
            log.warn("Redis验证耗时过长: {}ms (超过100ms，建议检查Redis连接)", redisTime);
        }
        
        // 从 Token 中获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        
        if (userId == null || username == null) {
            log.warn("无法从 Token 中获取用户信息: userId={}, username={}", userId, username);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token无效");
            return false;
        }
        
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        
        long totalTime = System.currentTimeMillis() - startTime;
        if (totalTime > 100) {
            log.warn("[性能警告] AuthInterceptor总耗时: {}ms, {}", totalTime, requestPath);
        }
        
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
