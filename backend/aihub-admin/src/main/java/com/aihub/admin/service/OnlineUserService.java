package com.aihub.admin.service;

import com.aihub.admin.dto.request.OnlineUserListRequest;
import com.aihub.admin.dto.response.OnlineUserResponse;
import com.aihub.common.web.dto.PageResult;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 在线用户服务接口
 */
public interface OnlineUserService {
    
    /**
     * 保存在线用户信息
     * @param userId 用户ID
     * @param username 用户名
     * @param token Token
     * @param expirationSeconds 过期时间（秒）
     * @param request HTTP请求
     */
    void saveOnlineUser(Long userId, String username, String token, long expirationSeconds, HttpServletRequest request);
    
    /**
     * 删除在线用户信息
     * @param userId 用户ID
     */
    void deleteOnlineUser(Long userId);
    
    /**
     * 根据Token删除在线用户信息
     * @param token Token
     */
    void deleteOnlineUserByToken(String token);
    
    /**
     * 获取在线用户列表
     * @param request 查询请求
     * @return 在线用户列表
     */
    PageResult<OnlineUserResponse> getOnlineUserList(OnlineUserListRequest request);
    
    /**
     * 强制用户下线
     * @param userId 用户ID
     */
    void forceOffline(Long userId);
}
