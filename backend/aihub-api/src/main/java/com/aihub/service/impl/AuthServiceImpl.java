package com.aihub.service.impl;

import com.aihub.dto.LoginRequest;
import com.aihub.dto.LoginResponse;
import com.aihub.dto.RefreshTokenRequest;
import com.aihub.dto.RegisterRequest;
import com.aihub.dto.ForgotPasswordRequest;
import com.aihub.dto.ResetPasswordRequest;
import com.aihub.entity.User;
import com.aihub.enums.UserRole;
import com.aihub.exception.BusinessException;
import com.aihub.mapper.UserMapper;
import com.aihub.service.AuthService;
import com.aihub.service.VerificationCodeService;
import com.aihub.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private VerificationCodeService verificationCodeService;
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 查找用户（支持用户名或邮箱登录）
        String usernameOrEmail = request.getUsernameOrEmail();
        User user = userMapper.findByUsername(usernameOrEmail);
        if (user == null) {
            user = userMapper.findByEmail(usernameOrEmail);
        }
        
        // 验证用户是否存在
        if (user == null || user.getIsDeleted() == 1) {
            log.warn("登录失败：用户不存在 - usernameOrEmail={}", usernameOrEmail);
            throw new BusinessException("用户名或密码错误");
        }
        
        // 验证用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账户已被禁用，请联系管理员");
        }
        
        // 验证密码
        String inputPassword = request.getPassword();
        String storedPassword = user.getPassword();
        
        // 调试日志（生产环境应移除）
        log.debug("密码验证: username={}, inputPasswordLength={}, storedPasswordLength={}, storedPasswordPrefix={}", 
                user.getUsername(), 
                inputPassword != null ? inputPassword.length() : 0,
                storedPassword != null ? storedPassword.length() : 0,
                storedPassword != null && storedPassword.length() > 10 ? storedPassword.substring(0, 10) + "..." : storedPassword);
        
        if (!passwordEncoder.matches(inputPassword, storedPassword)) {
            log.warn("密码验证失败: username={}", user.getUsername());
            throw new BusinessException("用户名或密码错误");
        }
        
        log.debug("密码验证成功: username={}", user.getUsername());
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(86400L); // 24小时（秒）
        
        // 用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setRole(user.getRole());
        
        // 设置角色描述
        UserRole role = UserRole.valueOf(user.getRole());
        userInfo.setRoleDescription(role.getDescription());
        
        response.setUser(userInfo);
        
        log.info("用户登录成功: username={}, role={}", user.getUsername(), user.getRole());
        
        return response;
    }
    
    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        // 验证刷新Token
        if (!jwtUtil.validateToken(request.getRefreshToken())) {
            throw new BusinessException("刷新Token无效或已过期");
        }
        
        // 检查是否为刷新Token
        if (!jwtUtil.isRefreshToken(request.getRefreshToken())) {
            throw new BusinessException("无效的刷新Token");
        }
        
        // 获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(request.getRefreshToken());
        String username = jwtUtil.getUsernameFromToken(request.getRefreshToken());
        
        if (userId == null || username == null) {
            throw new BusinessException("刷新Token无效");
        }
        
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账户已被禁用");
        }
        
        // 生成新的Token
        String newToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(newToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn(86400L); // 24小时（秒）
        
        // 用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setRole(user.getRole());
        
        UserRole role = UserRole.valueOf(user.getRole());
        userInfo.setRoleDescription(role.getDescription());
        
        response.setUser(userInfo);
        
        log.info("Token刷新成功: username={}", user.getUsername());
        
        return response;
    }
    
    @Override
    public void logout(String token) {
        // 这里可以实现Token黑名单机制
        // 目前JWT是无状态的，登出主要靠前端删除Token
        log.info("用户登出: token={}", token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null");
    }
    
    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
    
    @Override
    public void register(RegisterRequest request) {
        // 验证密码和确认密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(request.getUsername());
        if (existingUser != null && existingUser.getIsDeleted() == 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        User existingEmail = userMapper.findByEmail(request.getEmail());
        if (existingEmail != null && existingEmail.getIsDeleted() == 0) {
            throw new BusinessException("邮箱已被注册");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER.getCode()); // 注册用户默认为普通用户
        user.setStatus(1); // 默认启用
        user.setIsDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        log.info("用户注册成功: username={}, email={}", user.getUsername(), user.getEmail());
    }
    
    @Override
    public String sendForgotPasswordCode(ForgotPasswordRequest request) {
        // 检查邮箱是否存在
        User user = userMapper.findByEmail(request.getEmail());
        if (user == null || user.getIsDeleted() == 1) {
            // 为了安全，不暴露用户是否存在，统一返回成功
            log.warn("忘记密码请求：邮箱不存在 - email={}", request.getEmail());
            // 即使邮箱不存在，也生成验证码（防止邮箱枚举攻击）
            return verificationCodeService.generateAndSendCode(request.getEmail(), "reset");
        }
        
        // 生成验证码（开发模式会返回验证码，前端可以直接显示）
        String code = verificationCodeService.generateAndSendCode(request.getEmail(), "reset");
        log.info("忘记密码验证码已生成: email={}", request.getEmail());
        
        // 返回验证码（开发模式），前端可以直接显示
        return code;
    }
    
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        // 验证密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        
        // 验证验证码
        if (!verificationCodeService.verifyCode(request.getEmail(), request.getCode(), "reset")) {
            throw new BusinessException("验证码错误或已过期");
        }
        
        // 查找用户
        User user = userMapper.findByEmail(request.getEmail());
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("密码重置成功: email={}", request.getEmail());
    }
}
