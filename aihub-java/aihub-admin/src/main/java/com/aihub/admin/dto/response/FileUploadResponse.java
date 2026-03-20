package com.aihub.admin.dto.response;

import lombok.Data;

/**
 * 文件上传响应DTO
 */
@Data
public class FileUploadResponse {
    
    /**
     * 文件URL（用于访问文件）
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
}
