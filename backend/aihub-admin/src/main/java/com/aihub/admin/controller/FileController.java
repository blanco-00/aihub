package com.aihub.admin.controller;

import com.aihub.admin.annotation.OperationLog;
import com.aihub.admin.dto.response.FileListResponse;
import com.aihub.admin.dto.response.FileUploadResponse;
import com.aihub.admin.service.FileService;
import com.aihub.common.web.dto.PageResult;
import com.aihub.common.web.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileController {
    
    @Autowired
    private FileService fileService;
    
    /**
     * 上传文件
     * 
     * @param file 文件
     * @param category 文件分类（可选，如：avatar-头像, document-文档, image-图片）
     */
    @OperationLog(module = "文件管理", operation = "上传文件", recordParams = false)
    @PostMapping("/upload")
    public Result<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false, defaultValue = "image") String category) {
        try {
            log.info("文件上传请求: filename={}, size={}, category={}", 
                file.getOriginalFilename(), file.getSize(), category);
            
            FileUploadResponse response = fileService.uploadFile(file, category);
            return Result.success(response);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw e;
        }
    }
    
    /**
     * 获取文件列表
     * 
     * @param category 文件分类（可选）
     * @param keyword 搜索关键词（可选）
     * @param current 当前页（默认1）
     * @param size 每页大小（默认10）
     */
    @GetMapping("/list")
    public Result<PageResult<FileListResponse>> getFileList(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        try {
            log.info("获取文件列表: category={}, keyword={}, current={}, size={}", 
                category, keyword, current, size);
            
            PageResult<FileListResponse> result = fileService.getFileList(category, keyword, current, size);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            throw e;
        }
    }
    
    /**
     * 下载文件
     * 
     * @param url 文件URL
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("url") String url) {
        try {
            log.info("文件下载请求: url={}", url);
            
            byte[] fileBytes = fileService.downloadFile(url);
            
            // 从URL中提取文件名
            String filename = "file";
            if (url.contains("/")) {
                filename = url.substring(url.lastIndexOf("/") + 1);
            }
            
            // URL编码文件名
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileBytes);
        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw e;
        }
    }
    
    /**
     * 删除文件
     * 
     * @param url 文件URL
     */
    @OperationLog(module = "文件管理", operation = "删除文件", recordParams = true)
    @DeleteMapping
    public Result<Void> deleteFile(@RequestParam("url") String url) {
        try {
            log.info("文件删除请求: url={}", url);
            fileService.deleteFile(url);
            return Result.success();
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw e;
        }
    }
}
