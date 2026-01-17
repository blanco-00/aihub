package com.aihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知发布范围实体类
 */
@Data
@TableName("notice_scope")
public class NoticeScope {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("notice_id")
    private Long noticeId;
    
    @TableField("scope_type")
    private Integer scopeType;
    
    @TableField("scope_id")
    private Long scopeId;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}
