package com.aihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户通知关联实体类
 */
@Data
@TableName("notice_user")
public class NoticeUser {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("notice_id")
    private Long noticeId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("is_read")
    private Integer isRead;
    
    @TableField("read_time")
    private LocalDateTime readTime;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
