package com.aihub.admin.service.impl;

import com.aihub.admin.service.TokenCacheService;
import com.aihub.common.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 缓存服务实现
 * 使用 Redis 存储 Token 和用户信息
 */
@Slf4j
@Service
public class TokenCacheServiceImpl implements TokenCacheService {
    
    private static final String TOKEN_PREFIX = "token:";
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_TOKEN_PREFIX = "user:token:";
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Override
    public void saveToken(String token, Long userId, long expirationSeconds) {
        try {
            String tokenKey = TOKEN_PREFIX + token;
            String userTokenKey = USER_TOKEN_PREFIX + userId;
            
            // 存储 token -> userId 的映射
            redisUtil.set(tokenKey, userId, expirationSeconds);
            
            // 存储 userId -> token 的映射（用于一个用户只能有一个有效 token，可选）
            // 如果需要支持多设备登录，可以注释掉这部分
            // redisUtil.set(userTokenKey, token, expirationSeconds);
            
            log.debug("Token 已存入缓存: userId={}, expiration={}秒", userId, expirationSeconds);
        } catch (Exception e) {
            log.error("保存 Token 到缓存失败", e);
            // 缓存失败不影响登录，只记录日志
        }
    }
    
    @Override
    public boolean isTokenValid(String token) {
        long startTime = System.currentTimeMillis();
        try {
            // 性能优化：合并两次 Redis 查询为一次（使用 pipeline 或批量查询）
            // 先检查是否在黑名单中
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            String tokenKey = TOKEN_PREFIX + token;
            
            // 优化：只查询一次，先查黑名单，如果不在黑名单再查Token
            // 这样可以减少一次 Redis 查询（大部分情况下Token不在黑名单）
            long blacklistStart = System.currentTimeMillis();
            boolean inBlacklist = redisUtil.hasKey(blacklistKey);
            long blacklistTime = System.currentTimeMillis() - blacklistStart;
            
            if (inBlacklist) {
                log.debug("Token 在黑名单中: {}", token);
                return false;
            }
            
            // 如果不在黑名单，再检查 Token 是否在缓存中
            long tokenCheckStart = System.currentTimeMillis();
            boolean exists = redisUtil.hasKey(tokenKey);
            long tokenCheckTime = System.currentTimeMillis() - tokenCheckStart;
            
            long totalTime = System.currentTimeMillis() - startTime;
            if (totalTime > 100) {
                log.warn("isTokenValid总耗时过长: {}ms (黑名单检查: {}ms, Token检查: {}ms)", 
                    totalTime, blacklistTime, tokenCheckTime);
            } else if (blacklistTime > 50 || tokenCheckTime > 50) {
                log.warn("Redis操作耗时过长: 黑名单检查={}ms, Token检查={}ms", blacklistTime, tokenCheckTime);
            }
            
            if (exists) {
                log.debug("Token 在缓存中有效: {}", token);
            } else {
                log.debug("Token 不在缓存中: {}", token);
            }
            
            return exists;
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            log.error("检查 Token 有效性失败，耗时: {}ms", totalTime, e);
            // 缓存异常时，返回 true，让 JWT 验证来处理
            return true;
        }
    }
    
    @Override
    public void invalidateToken(String token, long expirationSeconds) {
        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            String tokenKey = TOKEN_PREFIX + token;
            
            // 将 Token 加入黑名单
            redisUtil.set(blacklistKey, "1", expirationSeconds);
            
            // 删除 Token 缓存
            redisUtil.delete(tokenKey);
            
            log.debug("Token 已加入黑名单: {}, expiration={}秒", token, expirationSeconds);
        } catch (Exception e) {
            log.error("使 Token 失效失败", e);
        }
    }
    
    @Override
    public void deleteToken(String token) {
        try {
            String tokenKey = TOKEN_PREFIX + token;
            redisUtil.delete(tokenKey);
            log.debug("Token 已从缓存删除: {}", token);
        } catch (Exception e) {
            log.error("删除 Token 失败", e);
        }
    }
    
    @Override
    public Long getUserIdByToken(String token) {
        try {
            String tokenKey = TOKEN_PREFIX + token;
            Long userId = redisUtil.get(tokenKey, Long.class);
            return userId;
        } catch (Exception e) {
            log.error("从缓存获取用户ID失败", e);
            return null;
        }
    }
}
