package com.aihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体类
 */
@Data
@TableName("login_log")
public class LoginLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    private String username;
    
    private String ip;
    
    private String address;
    
    @TableField("user_agent")
    private String userAgent;
    
    @TableField("nickname")
    private String nickname;

    @TableField("login_time")
    private LocalDateTime loginTime;
    
    /**
     * 登录状态: 1-成功, 0-失败
     */
    private Integer status;
    
    /**
     * 登录消息/备注
     */
    private String message;
}
