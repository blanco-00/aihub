package com.aihub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门实体类
 */
@Data
@TableName("department")
public class Department {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    @TableField("parent_id")
    private Long parentId;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    private Integer status;
    
    private String remark;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableField("is_deleted")
    private Integer isDeleted;
}
