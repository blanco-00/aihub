package com.aihub.service;

/**
 * Token 缓存服务接口
 * 用于管理 Token 的 Redis 缓存
 */
public interface TokenCacheService {
    
    /**
     * 将 Token 存入缓存
     * @param token Token 字符串
     * @param userId 用户ID
     * @param expirationSeconds 过期时间（秒）
     */
    void saveToken(String token, Long userId, long expirationSeconds);
    
    /**
     * 检查 Token 是否在缓存中（有效）
     * @param token Token 字符串
     * @return true 如果 Token 有效，false 如果 Token 无效或已过期
     */
    boolean isTokenValid(String token);
    
    /**
     * 使 Token 失效（加入黑名单）
     * @param token Token 字符串
     * @param expirationSeconds 黑名单过期时间（秒），通常与 Token 过期时间相同
     */
    void invalidateToken(String token, long expirationSeconds);
    
    /**
     * 从缓存中删除 Token
     * @param token Token 字符串
     */
    void deleteToken(String token);
    
    /**
     * 获取 Token 对应的用户ID
     * @param token Token 字符串
     * @return 用户ID，如果 Token 不存在则返回 null
     */
    Long getUserIdByToken(String token);
}
