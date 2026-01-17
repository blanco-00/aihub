package com.aihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件实体类
 */
@Data
@TableName("file")
public class File {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("filename")
    private String filename;
    
    @TableField("storage_filename")
    private String storageFilename;
    
    @TableField("file_path")
    private String filePath;
    
    @TableField("file_url")
    private String fileUrl;
    
    @TableField("file_size")
    private Long fileSize;
    
    @TableField("content_type")
    private String contentType;
    
    private String category;
    
    @TableField("upload_user_id")
    private Long uploadUserId;
    
    @TableField("upload_username")
    private String uploadUsername;
    
    @TableField("business_type")
    private String businessType;
    
    @TableField("business_id")
    private Long businessId;
    
    @TableField("reference_count")
    private Integer referenceCount;
    
    private Integer status;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableField("is_deleted")
    private Integer isDeleted;
}
