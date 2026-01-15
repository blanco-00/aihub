package com.aihub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单实体类
 */
@Data
@TableName("menu")
public class Menu {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("parent_id")
    private Long parentId;
    
    private String name;
    
    private String path;
    
    private String component;
    
    private String redirect;
    
    private String icon;
    
    private String title;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    @TableField("show_link")
    private Integer showLink;
    
    @TableField("keep_alive")
    private Integer keepAlive;
    
    private Integer status;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableField("is_deleted")
    private Integer isDeleted;
}
