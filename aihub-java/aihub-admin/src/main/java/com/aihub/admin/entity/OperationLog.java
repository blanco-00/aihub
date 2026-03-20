package com.aihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@TableName("operation_log")
public class OperationLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    private String username;
    
    private String module;
    
    private String operation;
    
    private String method;
    
    private String url;
    
    private String params;
    
    private String result;
    
    private Integer status;
    
    private String ip;
    
    private Integer duration;
    
    @TableField("operation_time")
    private LocalDateTime operationTime;
}
