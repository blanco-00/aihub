package com.aihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Prompt模板实体类
 */
@Data
@TableName("prompt_template")
public class PromptTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 变量定义（JSON格式）
     */
    private String variables;

    /**
     * 是否内置 0-否 1-是
     */
    private Integer isBuiltin;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("is_deleted")
    private Integer isDeleted;
}
