package com.aihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统日志实体类
 */
@Data
@TableName("system_log")
public class SystemLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String level; // 日志级别 DEBUG/INFO/WARN/ERROR
    
    private String module; // 模块名称
    
    private String message; // 日志消息
    
    @TableField("stack_trace")
    private String stackTrace; // 堆栈信息
    
    private String ip; // IP地址
    
    @TableField("user_id")
    private Long userId; // 用户ID
    
    @TableField("request_id")
    private String requestId; // 请求ID
    
    @TableField("log_time")
    private LocalDateTime logTime; // 日志时间
}
