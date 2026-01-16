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
    
    private Integer status;
    
    private String message;
    
    @TableField("login_time")
    private LocalDateTime loginTime;
}
