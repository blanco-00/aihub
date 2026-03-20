package com.aihub.admin.service.impl;

import com.aihub.admin.dto.request.OnlineUserListRequest;
import com.aihub.admin.dto.response.OnlineUserResponse;
import com.aihub.admin.service.OnlineUserService;
import com.aihub.admin.service.TokenCacheService;
import com.aihub.admin.utils.UserAgentUtils;
import com.aihub.common.redis.RedisUtil;
import com.aihub.common.web.dto.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 在线用户服务实现
 */
@Slf4j
@Service
public class OnlineUserServiceImpl implements OnlineUserService {
    
    private static final String ONLINE_USER_PREFIX = "online:user:";
    private static final String FORCE_OFFLINE_USER_PREFIX = "force:offline:user:";
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private TokenCacheService tokenCacheService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "未知";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
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
    
    @Override
    public void saveOnlineUser(Long userId, String username, String token, long expirationSeconds, HttpServletRequest request) {
        try {
            String key = ONLINE_USER_PREFIX + userId;
            
            // 获取IP地址
            String ip = getClientIpAddress(request);
            
            // 获取User-Agent
            String userAgent = request != null ? request.getHeader("User-Agent") : null;
            String system = UserAgentUtils.parseOS(userAgent);
            String browser = UserAgentUtils.parseBrowser(userAgent);
            
            // 构建在线用户信息
            Map<String, Object> onlineUserInfo = new HashMap<>();
            onlineUserInfo.put("userId", userId);
            onlineUserInfo.put("username", username);
            onlineUserInfo.put("ip", ip);
            onlineUserInfo.put("address", ip); // 简化处理，直接使用IP作为地址
            onlineUserInfo.put("system", system);
            onlineUserInfo.put("browser", browser);
            onlineUserInfo.put("loginTime", LocalDateTime.now().toString());
            onlineUserInfo.put("token", token);
            
            // 存储到Redis，设置过期时间
            redisUtil.set(key, onlineUserInfo, expirationSeconds, TimeUnit.SECONDS);
            
            log.debug("保存在线用户信息成功: userId={}, username={}, ip={}", userId, username, ip);
        } catch (Exception e) {
            log.error("保存在线用户信息失败: userId={}, username={}", userId, username, e);
            // 不影响登录流程，只记录日志
        }
    }
    
    @Override
    public void deleteOnlineUser(Long userId) {
        try {
            String key = ONLINE_USER_PREFIX + userId;
            redisUtil.delete(key);
            log.debug("删除在线用户信息成功: userId={}", userId);
        } catch (Exception e) {
            log.error("删除在线用户信息失败: userId={}", userId, e);
        }
    }
    
    @Override
    public void deleteOnlineUserByToken(String token) {
        try {
            Long userId = tokenCacheService.getUserIdByToken(token);
            if (userId != null) {
                deleteOnlineUser(userId);
            }
        } catch (Exception e) {
            log.error("根据Token删除在线用户信息失败: token={}", token, e);
        }
    }
    
    @Override
    public PageResult<OnlineUserResponse> getOnlineUserList(OnlineUserListRequest request) {
        try {
            List<OnlineUserResponse> onlineUsers = new ArrayList<>();
            
            // 使用SCAN命令扫描所有在线用户（避免KEYS命令阻塞）
            if (redisTemplate != null) {
                ScanOptions options = ScanOptions.scanOptions()
                        .match(ONLINE_USER_PREFIX + "*")
                        .count(100)
                        .build();
                
                try (Cursor<String> cursor = redisTemplate.scan(options)) {
                    while (cursor.hasNext()) {
                        String key = cursor.next();
                        
                        // 获取在线用户信息
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userInfo = redisUtil.get(key, Map.class);
                        if (userInfo == null) {
                            continue;
                        }
                        
                        // 转换为响应对象
                        OnlineUserResponse response = convertToResponse(userInfo);
                        if (response == null) {
                            continue;
                        }
                        
                        // 过滤用户名（如果指定了）
                        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
                            if (response.getUsername() == null || 
                                !response.getUsername().contains(request.getUsername())) {
                                continue;
                            }
                        }
                        
                        onlineUsers.add(response);
                    }
                }
            }
            
            // 内存分页
            int total = onlineUsers.size();
            int current = request.getCurrent() != null ? request.getCurrent() : 1;
            int size = request.getSize() != null ? request.getSize() : 10;
            int start = (current - 1) * size;
            int end = Math.min(start + size, total);
            
            List<OnlineUserResponse> pagedList = start < total ? onlineUsers.subList(start, end) : new ArrayList<>();
            
            // 计算总页数
            long pages = (total + size - 1) / size;
            
            // 构建分页结果
            PageResult<OnlineUserResponse> result = new PageResult<>();
            result.setRecords(pagedList);
            result.setTotal((long) total);
            result.setCurrent(current);
            result.setSize(size);
            result.setPages(pages);
            
            return result;
        } catch (Exception e) {
            log.error("获取在线用户列表失败", e);
            // 返回空结果，避免接口报错
            PageResult<OnlineUserResponse> result = new PageResult<>();
            result.setRecords(new ArrayList<>());
            result.setTotal(0L);
            result.setCurrent(request.getCurrent() != null ? request.getCurrent() : 1);
            result.setSize(request.getSize() != null ? request.getSize() : 10);
            result.setPages(0L);
            return result;
        }
    }
    
    @Override
    public void forceOffline(Long userId) {
        try {
            // 获取在线用户信息，找到对应的Token
            String key = ONLINE_USER_PREFIX + userId;
            @SuppressWarnings("unchecked")
            Map<String, Object> userInfo = redisUtil.get(key, Map.class);
            
            String token = null;
            long expirationSeconds = 7L * 24 * 60 * 60; // 默认7天
            
            if (userInfo != null) {
                token = (String) userInfo.get("token");
                // 获取Token的剩余过期时间
                long expire = redisUtil.getExpire(key);
                if (expire > 0) {
                    expirationSeconds = expire;
                }
            }
            
            // 如果从在线用户信息中无法获取Token，尝试从Token缓存中查找
            if (token == null) {
                log.warn("在线用户信息中没有Token，尝试从Token缓存中查找: userId={}", userId);
                // 扫描所有 token:* 的key，找到值为该userId的token
                token = findTokenByUserId(userId);
                if (token != null) {
                    // 获取Token缓存的剩余过期时间
                    String tokenKey = "token:" + token;
                    long expire = redisUtil.getExpire(tokenKey);
                    if (expire > 0) {
                        expirationSeconds = expire;
                    }
                }
            }
            
            // 使Token失效
            if (token != null) {
                tokenCacheService.invalidateToken(token, expirationSeconds);
                log.info("Token已加入黑名单: userId={}, token={}, expirationSeconds={}", 
                    userId, token.substring(0, Math.min(20, token.length())) + "...", expirationSeconds);
            } else {
                log.warn("无法找到用户的Token，可能已经过期: userId={}", userId);
            }
            
            // 删除在线用户信息
            deleteOnlineUser(userId);
            
            // 将用户ID加入强制下线黑名单（防止用户通过刷新Token重新登录）
            String forceOfflineKey = FORCE_OFFLINE_USER_PREFIX + userId;
            // 设置7天的过期时间，确保即使Token过期，用户也无法刷新
            redisUtil.set(forceOfflineKey, "1", 7L * 24 * 60 * 60, TimeUnit.SECONDS);
            log.info("用户已加入强制下线黑名单: userId={}", userId);
            
            log.info("强制用户下线成功: userId={}", userId);
        } catch (Exception e) {
            log.error("强制用户下线失败: userId={}", userId, e);
            throw new RuntimeException("强制用户下线失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据用户ID查找Token（通过扫描Token缓存）
     */
    private String findTokenByUserId(Long userId) {
        try {
            if (redisTemplate == null) {
                return null;
            }
            
            ScanOptions options = ScanOptions.scanOptions()
                    .match("token:*")
                    .count(100)
                    .build();
            
            try (Cursor<String> cursor = redisTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    String tokenKey = cursor.next();
                    // 跳过黑名单key
                    if (tokenKey.contains("blacklist")) {
                        continue;
                    }
                    
                    // 获取Token对应的userId
                    Object value = redisUtil.get(tokenKey);
                    if (value != null) {
                        Long tokenUserId = null;
                        if (value instanceof Number) {
                            tokenUserId = ((Number) value).longValue();
                        } else if (value instanceof String) {
                            try {
                                tokenUserId = Long.parseLong((String) value);
                            } catch (NumberFormatException e) {
                                // 忽略
                            }
                        }
                        
                        if (tokenUserId != null && tokenUserId.equals(userId)) {
                            // 找到对应的Token，提取token字符串
                            String token = tokenKey.substring("token:".length());
                            log.debug("找到用户的Token: userId={}, token={}", userId, token.substring(0, Math.min(20, token.length())) + "...");
                            return token;
                        }
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("查找用户Token失败: userId={}", userId, e);
            return null;
        }
    }
    
    /**
     * 转换为响应对象
     */
    private OnlineUserResponse convertToResponse(Map<String, Object> userInfo) {
        try {
            OnlineUserResponse response = new OnlineUserResponse();
            
            Object userIdObj = userInfo.get("userId");
            if (userIdObj instanceof Number) {
                response.setUserId(((Number) userIdObj).longValue());
            }
            
            response.setUsername((String) userInfo.get("username"));
            response.setIp((String) userInfo.get("ip"));
            response.setAddress((String) userInfo.get("address"));
            response.setSystem((String) userInfo.get("system"));
            response.setBrowser((String) userInfo.get("browser"));
            
            String loginTimeStr = (String) userInfo.get("loginTime");
            if (loginTimeStr != null) {
                response.setLoginTime(LocalDateTime.parse(loginTimeStr));
            }
            
            response.setToken((String) userInfo.get("token"));
            
            return response;
        } catch (Exception e) {
            log.warn("转换在线用户信息失败: userInfo={}", userInfo, e);
            return null;
        }
    }
}
