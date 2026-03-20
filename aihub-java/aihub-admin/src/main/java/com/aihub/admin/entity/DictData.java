package com.aihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典数据实体类
 */
@Data
@TableName("dict_data")
public class DictData {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("dict_type")
    private String dictType;
    
    @TableField("dict_label")
    private String dictLabel;
    
    @TableField("dict_value")
    private String dictValue;
    
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
