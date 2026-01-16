package com.aihub.admin.controller;

import com.aihub.common.web.dto.Result;
import com.aihub.admin.dto.request.LoginRequest;
import com.aihub.admin.dto.response.LoginResponse;
import com.aihub.admin.dto.request.RefreshTokenRequest;
import com.aihub.admin.dto.request.RegisterRequest;
import com.aihub.admin.dto.request.ForgotPasswordRequest;
import com.aihub.admin.dto.response.ForgotPasswordResponse;
import com.aihub.admin.dto.request.ResetPasswordRequest;
import com.aihub.admin.dto.response.UserListResponse;
import com.aihub.common.web.exception.BusinessException;
import com.aihub.admin.service.AuthService;
import com.aihub.admin.service.LoginLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private com.aihub.admin.service.UserService userService;
    
    @Autowired
    private LoginLogService loginLogService;
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public Result<UserListResponse> getCurrentUser(
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            // 从拦截器设置的属性中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error(401, "未登录或Token无效");
            }
            
            UserListResponse user = userService.getUserById(userId);
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            throw e;
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String usernameOrEmail = request.getUsernameOrEmail();
        try {
            log.info("用户登录请求: usernameOrEmail={}", usernameOrEmail);
            LoginResponse response = authService.login(request);
            
            // 记录登录成功日志
            if (response.getUser() != null) {
                loginLogService.recordLogin(
                    response.getUser().getId(),
                    response.getUser().getUsername(),
                    1,
                    "登录成功",
                    httpRequest
                );
            }
            
            return Result.success(response);
        } catch (BusinessException e) {
            // 记录登录失败日志
            loginLogService.recordLogin(
                null,
                usernameOrEmail,
                0,
                "登录失败: " + e.getMessage(),
                httpRequest
            );
            log.error("登录失败", e);
            throw e;
        } catch (Exception e) {
            // 记录登录失败日志
            loginLogService.recordLogin(
                null,
                usernameOrEmail,
                0,
                "登录失败: " + e.getMessage(),
                httpRequest
            );
            log.error("登录失败", e);
            throw e;
        }
    }
    
    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            log.info("刷新Token请求");
            LoginResponse response = authService.refreshToken(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("刷新Token失败", e);
            throw e;
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            String token = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            }
            authService.logout(token);
            return Result.success();
        } catch (Exception e) {
            log.error("登出失败", e);
            throw e;
        }
    }
    
    /**
     * 验证Token
     */
    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            String token = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            }
            boolean valid = token != null && authService.validateToken(token);
            return Result.success(valid);
        } catch (Exception e) {
            log.error("验证Token失败", e);
            return Result.success(false);
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("用户注册请求: username={}, email={}", request.getUsername(), request.getEmail());
            authService.register(request);
            return Result.success();
        } catch (Exception e) {
            log.error("注册失败", e);
            throw e;
        }
    }
    
    /**
     * 发送忘记密码验证码
     */
    @PostMapping("/forgot-password")
    public Result<ForgotPasswordResponse> sendForgotPasswordCode(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            log.info("忘记密码请求: email={}", request.getEmail());
            String code = authService.sendForgotPasswordCode(request);
            
            // 构建响应（开发模式返回验证码）
            ForgotPasswordResponse response = new ForgotPasswordResponse();
            response.setCode(code); // 开发模式返回验证码，前端可以直接显示
            response.setMessage("验证码已生成，请查看页面提示（开发模式）");
            
            // 为了安全，无论邮箱是否存在都返回成功
            return Result.success(response);
        } catch (Exception e) {
            log.error("发送忘记密码验证码失败", e);
            throw e;
        }
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            log.info("重置密码请求: email={}", request.getEmail());
            authService.resetPassword(request);
            return Result.success();
        } catch (Exception e) {
            log.error("重置密码失败", e);
            throw e;
        }
    }
}
