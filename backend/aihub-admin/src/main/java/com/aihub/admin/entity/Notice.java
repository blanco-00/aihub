package com.aihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知公告实体类
 */
@Data
@TableName("notice")
public class Notice {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String content;
    
    @TableField("category_id")
    private Long categoryId;
    
    private Integer type;
    
    @TableField("publish_type")
    private Integer publishType;
    
    @TableField("publisher_id")
    private Long publisherId;
    
    @TableField("publisher_name")
    private String publisherName;
    
    private Integer status;
    
    @TableField("publish_time")
    private LocalDateTime publishTime;
    
    @TableField("expire_time")
    private LocalDateTime expireTime;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    @TableField("view_count")
    private Integer viewCount;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableField("is_deleted")
    private Integer isDeleted;
}
