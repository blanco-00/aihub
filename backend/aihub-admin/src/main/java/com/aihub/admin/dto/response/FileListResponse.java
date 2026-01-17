package com.aihub.admin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件列表响应DTO
 */
@Data
public class FileListResponse {
    
    /**
     * 文件ID
     */
    private Long id;
    
    /**
     * 文件URL
     */
    private String url;
    
    /**
     * 文件名称
     */
    private String filename;
    
    /**
     * 文件大小（字节）
     */
    private Long size;
    
    /**
     * 文件类型（MIME类型）
     */
    private String contentType;
    
    /**
     * 文件分类
     */
    private String category;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
    
    /**
     * 文件路径（相对路径）
     */
    private String path;
    
    /**
     * 上传用户名
     */
    private String uploadUsername;
    
    /**
     * 引用次数
     */
    private Integer referenceCount;
}
