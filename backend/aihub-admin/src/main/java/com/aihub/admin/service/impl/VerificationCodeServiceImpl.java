package com.aihub.admin.service.impl;

import com.aihub.admin.service.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 验证码服务实现
 * 注意：当前使用内存存储验证码，生产环境建议使用Redis
 */
@Slf4j
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {
    
    // 验证码存储（key: email_type, value: code_info）
    private final Map<String, CodeInfo> codeStorage = new ConcurrentHashMap<>();
    
    @Value("${verification.code.expiration:300000}") // 默认5分钟（毫秒）
    private Long codeExpiration;
    
    @Value("${verification.code.length:6}") // 默认6位
    private Integer codeLength;
    
    @Value("${verification.code.dev-mode:true}") // 开发模式：打印验证码到日志
    private Boolean devMode;
    
    @Override
    public String generateAndSendCode(String email, String type) {
        // 生成6位数字验证码
        String code = generateCode();
        
        // 存储验证码
        String key = email + "_" + type;
        CodeInfo codeInfo = new CodeInfo(code, System.currentTimeMillis() + codeExpiration);
        codeStorage.put(key, codeInfo);
        
        // 开发模式：打印验证码到日志
        if (devMode) {
            log.info("验证码已生成（开发模式）: email={}, type={}, code={}, expiration={}秒", 
                    email, type, code, codeExpiration / 1000);
        }
        
        // 注意：当前实现不发送邮件，验证码直接返回给前端（开发模式）
        // 生产环境建议：
        // 1. 使用免费的邮件服务（如QQ邮箱、163邮箱的SMTP服务）
        // 2. 或使用第三方邮件服务（如SendGrid、AWS SES等，可能收费）
        // 3. 或使用短信验证码服务
        
        // 返回验证码给前端（开发模式），前端可以直接显示
        return code;
    }
    
    @Override
    public boolean verifyCode(String email, String code, String type) {
        String key = email + "_" + type;
        CodeInfo codeInfo = codeStorage.get(key);
        
        if (codeInfo == null) {
            log.warn("验证码不存在: email={}, type={}", email, type);
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > codeInfo.getExpirationTime()) {
            log.warn("验证码已过期: email={}, type={}", email, type);
            codeStorage.remove(key);
            return false;
        }
        
        // 验证验证码
        boolean valid = codeInfo.getCode().equals(code);
        if (valid) {
            log.info("验证码验证成功: email={}, type={}", email, type);
            // 验证成功后清除验证码
            codeStorage.remove(key);
        } else {
            log.warn("验证码错误: email={}, type={}, inputCode={}", email, type, code);
        }
        
        return valid;
    }
    
    @Override
    public void clearCode(String email, String type) {
        String key = email + "_" + type;
        codeStorage.remove(key);
        log.debug("验证码已清除: email={}, type={}", email, type);
    }
    
    /**
     * 生成验证码
     */
    private String generateCode() {
        int min = (int) Math.pow(10, codeLength - 1);
        int max = (int) Math.pow(10, codeLength) - 1;
        int code = ThreadLocalRandom.current().nextInt(min, max + 1);
        return String.valueOf(code);
    }
    
    /**
     * 验证码信息
     */
    private static class CodeInfo {
        private final String code;
        private final long expirationTime;
        
        public CodeInfo(String code, long expirationTime) {
            this.code = code;
            this.expirationTime = expirationTime;
        }
        
        public String getCode() {
            return code;
        }
        
        public long getExpirationTime() {
            return expirationTime;
        }
    }
}
