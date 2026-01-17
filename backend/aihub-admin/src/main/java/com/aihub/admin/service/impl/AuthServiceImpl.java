package com.aihub.admin.service.impl;

import com.aihub.admin.dto.request.LoginRequest;
import com.aihub.admin.dto.response.LoginResponse;
import com.aihub.admin.dto.request.RefreshTokenRequest;
import com.aihub.admin.dto.request.RegisterRequest;
import com.aihub.admin.dto.request.ForgotPasswordRequest;
import com.aihub.admin.dto.request.ResetPasswordRequest;
import com.aihub.admin.dto.request.LoginLogListRequest;
import com.aihub.admin.dto.request.OperationLogListRequest;
import com.aihub.admin.dto.response.LoginLogResponse;
import com.aihub.admin.dto.response.OperationLogResponse;
import com.aihub.admin.dto.response.SecurityLogResponse;
import com.aihub.admin.entity.User;
import com.aihub.admin.enums.UserRole;
import com.aihub.admin.utils.UserAgentUtils;
import com.aihub.common.web.dto.PageResult;
import com.aihub.common.web.exception.BusinessException;
import com.aihub.admin.mapper.UserMapper;
import com.aihub.admin.mapper.LoginLogMapper;
import com.aihub.admin.mapper.OperationLogMapper;
import com.aihub.admin.service.AuthService;
import com.aihub.admin.service.TokenCacheService;
import com.aihub.admin.service.VerificationCodeService;
import com.aihub.common.redis.RedisUtil;
import com.aihub.common.security.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    
    @Autowired
    private TokenCacheService tokenCacheService;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private LoginLogMapper loginLogMapper;
    
    @Autowired
    private OperationLogMapper operationLogMapper;
    
    @Value("${jwt.expiration:86400000}")
    private Long tokenExpiration; // Token过期时间（毫秒）
    
    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshTokenExpiration; // 刷新Token过期时间（毫秒）
    
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
        
        // 根据 rememberMe 计算 Token 过期时间
        long tokenExpirationMillis;
        long expiresInSeconds;
        
        if (request.getRememberMe() != null && request.getRememberMe()) {
            // 勾选了免登录，固定使用30天
            tokenExpirationMillis = 30L * 24 * 60 * 60 * 1000; // 30天转换为毫秒
            expiresInSeconds = 30L * 24 * 60 * 60; // 30天转换为秒
            log.info("用户勾选了免登录: username={}, 使用30天过期时间", user.getUsername());
        } else {
            // 未勾选免登录，使用默认过期时间（24小时）
            tokenExpirationMillis = tokenExpiration;
            expiresInSeconds = tokenExpiration / 1000; // 转换为秒
            log.info("用户未勾选免登录: username={}, 使用默认过期时间24小时", user.getUsername());
        }
        
        // 生成Token（使用计算出的过期时间）
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), tokenExpirationMillis);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(expiresInSeconds);
        
        // 用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setRole(user.getRole());
        
        // 设置角色描述
        UserRole role = UserRole.valueOf(user.getRole());
        userInfo.setRoleDescription(role.getDescription());
        
        response.setUser(userInfo);
        
        // 将 Token 存入 Redis 缓存（使用计算出的过期时间）
        tokenCacheService.saveToken(token, user.getId(), expiresInSeconds);
        
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
        
        // 检查用户是否被强制下线
        String forceOfflineKey = "force:offline:user:" + userId;
        if (redisUtil.hasKey(forceOfflineKey)) {
            log.warn("用户尝试刷新Token，但已被强制下线: userId={}, username={}", userId, username);
            throw new BusinessException("账户已被强制下线，请重新登录");
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
        userInfo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setRole(user.getRole());
        
        UserRole role = UserRole.valueOf(user.getRole());
        userInfo.setRoleDescription(role.getDescription());
        
        response.setUser(userInfo);
        
        // 将旧的 refreshToken 加入黑名单（使其失效）
        long refreshExpirationSeconds = refreshTokenExpiration / 1000; // 转换为秒
        tokenCacheService.invalidateToken(request.getRefreshToken(), refreshExpirationSeconds);
        
        // 将新的 Token 存入 Redis 缓存
        long expirationSeconds = tokenExpiration / 1000; // 转换为秒
        tokenCacheService.saveToken(newToken, user.getId(), expirationSeconds);
        
        log.info("Token刷新成功: username={}", user.getUsername());
        
        return response;
    }
    
    @Override
    public void logout(String token) {
        if (token != null && !token.isEmpty()) {
            // 将 Token 加入黑名单，使其立即失效
            long expirationSeconds = tokenExpiration / 1000; // 转换为秒
            tokenCacheService.invalidateToken(token, expirationSeconds);
            log.info("用户登出: Token已加入黑名单, token={}", token.substring(0, Math.min(20, token.length())) + "...");
        } else {
            log.warn("用户登出: Token为空");
        }
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
    
    @Override
    public void updateProfile(Long userId, String nickname, String email, String phone, String description, String avatar) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        // 如果邮箱有变化，检查新邮箱是否已被其他用户使用
        if (email != null && !email.equals(user.getEmail())) {
            User existingUser = userMapper.findByEmail(email);
            if (existingUser != null && existingUser.getIsDeleted() == 0 && !existingUser.getId().equals(userId)) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
        }
        
        // 更新用户信息
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (description != null) {
            user.setDescription(description);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.updateById(user);
        log.info("个人信息更新成功: userId={}, nickname={}", userId, nickname);
    }
    
    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("当前密码错误");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("密码修改成功: userId={}", userId);
    }
    
    @Override
    public PageResult<SecurityLogResponse> getSecurityLogs(Long userId, Integer current, Integer size) {
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        String username = user.getUsername();
        
        // 查询登录日志（扩大查询范围，后续再筛选）
        LoginLogListRequest loginLogRequest = new LoginLogListRequest();
        loginLogRequest.setUsername(username);
        List<LoginLogResponse> allLoginLogs = loginLogMapper.selectLoginLogList(
            loginLogRequest, 0L, 1000); // 查询最多1000条
        
        // 查询操作日志（扩大查询范围，后续再筛选）
        OperationLogListRequest operationLogRequest = new OperationLogListRequest();
        operationLogRequest.setUsername(username);
        List<OperationLogResponse> allOperationLogs = operationLogMapper.selectOperationLogList(
            operationLogRequest, 0L, 1000); // 查询最多1000条
        
        // 转换为安全日志响应
        List<SecurityLogResponse> securityLogs = new ArrayList<>();
        
        // 转换登录日志
        for (LoginLogResponse loginLog : allLoginLogs) {
            if (loginLog.getUserId() != null && loginLog.getUserId().equals(userId)) {
                SecurityLogResponse securityLog = new SecurityLogResponse();
                securityLog.setId(loginLog.getId());
                securityLog.setType("LOGIN");
                securityLog.setSummary(loginLog.getMessage() != null ? loginLog.getMessage() : 
                    (loginLog.getStatus() != null && loginLog.getStatus() == 1 ? "登录成功" : "登录失败"));
                securityLog.setIp(loginLog.getIp());
                securityLog.setAddress(loginLog.getAddress());
                securityLog.setSystem(UserAgentUtils.parseOS(loginLog.getUserAgent()));
                securityLog.setBrowser(UserAgentUtils.parseBrowser(loginLog.getUserAgent()));
                securityLog.setOperatingTime(loginLog.getLoginTime());
                securityLogs.add(securityLog);
            }
        }
        
        // 转换操作日志
        for (OperationLogResponse operationLog : allOperationLogs) {
            if (operationLog.getUserId() != null && operationLog.getUserId().equals(userId)) {
                SecurityLogResponse securityLog = new SecurityLogResponse();
                securityLog.setId(operationLog.getId());
                securityLog.setType("OPERATION");
                String summary = (operationLog.getModule() != null ? operationLog.getModule() : "") + 
                    " - " + (operationLog.getOperation() != null ? operationLog.getOperation() : "");
                securityLog.setSummary(summary);
                securityLog.setIp(operationLog.getIp());
                securityLog.setAddress(null); // 操作日志可能没有地址信息
                securityLog.setSystem("未知"); // 操作日志可能没有User-Agent
                securityLog.setBrowser("未知");
                securityLog.setOperatingTime(operationLog.getOperationTime());
                securityLogs.add(securityLog);
            }
        }
        
        // 按时间倒序排序
        securityLogs.sort((a, b) -> {
            if (a.getOperatingTime() == null && b.getOperatingTime() == null) {
                return 0;
            }
            if (a.getOperatingTime() == null) {
                return 1;
            }
            if (b.getOperatingTime() == null) {
                return -1;
            }
            return b.getOperatingTime().compareTo(a.getOperatingTime());
        });
        
        // 分页处理
        int total = securityLogs.size();
        int fromIndex = (current - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);
        List<SecurityLogResponse> pagedLogs = fromIndex < total ? 
            securityLogs.subList(fromIndex, toIndex) : new ArrayList<>();
        
        // 构建分页结果
        PageResult<SecurityLogResponse> result = new PageResult<>();
        result.setRecords(pagedLogs);
        result.setTotal((long) total);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages((long) ((total + size - 1) / size));
        
        return result;
    }
}
