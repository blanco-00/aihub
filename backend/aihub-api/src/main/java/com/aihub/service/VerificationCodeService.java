package com.aihub.service;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {
    
    /**
     * 生成并发送验证码
     * @param email 邮箱地址
     * @param type 验证码类型（register-注册, reset-重置密码）
     * @return 验证码（开发环境返回，生产环境不返回）
     */
    String generateAndSendCode(String email, String type);
    
    /**
     * 验证验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @param type 验证码类型
     * @return 是否验证成功
     */
    boolean verifyCode(String email, String code, String type);
    
    /**
     * 清除验证码（验证成功后清除）
     * @param email 邮箱地址
     * @param type 验证码类型
     */
    void clearCode(String email, String type);
}
