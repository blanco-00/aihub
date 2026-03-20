package com.aihub.admin.service;

import com.aihub.admin.dto.request.LoginRequest;
import com.aihub.admin.dto.response.LoginResponse;
import com.aihub.admin.dto.request.RefreshTokenRequest;
import com.aihub.admin.dto.request.RegisterRequest;
import com.aihub.admin.dto.request.ForgotPasswordRequest;
import com.aihub.admin.dto.request.ResetPasswordRequest;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录响应（包含Token和用户信息）
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 用户注册
     * @param request 注册请求
     */
    void register(RegisterRequest request);
    
    /**
     * 发送忘记密码验证码
     * @param request 忘记密码请求
     * @return 验证码（仅开发模式返回，生产环境返回null）
     */
    String sendForgotPasswordCode(ForgotPasswordRequest request);
    
    /**
     * 重置密码
     * @param request 重置密码请求
     */
    void resetPassword(ResetPasswordRequest request);
    
    /**
     * 刷新Token
     * @param request 刷新Token请求
     * @return 新的Token信息
     */
    LoginResponse refreshToken(RefreshTokenRequest request);
    
    /**
     * 用户登出
     * @param token 当前Token
     */
    void logout(String token);
    
    /**
     * 验证Token是否有效
     * @param token Token字符串
     * @return 是否有效
     */
    boolean validateToken(String token);
    
    /**
     * 更新个人信息
     * @param userId 用户ID
     * @param nickname 昵称
     * @param email 邮箱
     * @param phone 手机号
     * @param description 简介
     * @param avatar 头像URL
     */
    void updateProfile(Long userId, String nickname, String email, String phone, String description, String avatar);
    
    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 获取当前用户的安全日志（合并登录日志和操作日志）
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 安全日志分页结果
     */
    com.aihub.common.web.dto.PageResult<com.aihub.admin.dto.response.SecurityLogResponse> getSecurityLogs(Long userId, Integer current, Integer size);
}
