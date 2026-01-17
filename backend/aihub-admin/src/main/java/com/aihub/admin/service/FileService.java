package com.aihub.admin.service;

import com.aihub.admin.dto.response.FileListResponse;
import com.aihub.admin.dto.response.FileUploadResponse;
import com.aihub.common.web.dto.PageResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 上传文件
     * 
     * @param file 文件
     * @param category 文件分类（如：avatar-头像, document-文档, image-图片）
     * @return 文件上传响应
     */
    FileUploadResponse uploadFile(MultipartFile file, String category);
    
    /**
     * 删除文件
     * 
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);
    
    /**
     * 获取文件列表
     * 
     * @param category 文件分类（可选）
     * @param keyword 搜索关键词（文件名）
     * @param current 当前页
     * @param size 每页大小
     * @return 文件列表
     */
    PageResult<FileListResponse> getFileList(String category, String keyword, Long current, Integer size);
    
    /**
     * 下载文件
     * 
     * @param fileUrl 文件URL
     * @return 文件字节数组
     */
    byte[] downloadFile(String fileUrl);
}
