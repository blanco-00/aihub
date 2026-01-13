package com.aihub.service;

import com.aihub.dto.LoginRequest;
import com.aihub.dto.LoginResponse;
import com.aihub.dto.RefreshTokenRequest;
import com.aihub.dto.RegisterRequest;
import com.aihub.dto.ForgotPasswordRequest;
import com.aihub.dto.ResetPasswordRequest;

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
}
