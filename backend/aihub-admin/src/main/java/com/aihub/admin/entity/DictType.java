package com.aihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典类型实体类
 */
@Data
@TableName("dict_type")
public class DictType {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("dict_name")
    private String dictName;
    
    @TableField("dict_type")
    private String dictType;
    
    private Integer status;
    
    private String remark;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableField("is_deleted")
    private Integer isDeleted;
}
