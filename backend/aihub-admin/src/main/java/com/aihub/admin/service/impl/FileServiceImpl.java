package com.aihub.admin.service.impl;

import com.aihub.admin.dto.response.FileListResponse;
import com.aihub.admin.dto.response.FileUploadResponse;
import com.aihub.admin.entity.File;
import com.aihub.admin.mapper.FileMapper;
import com.aihub.admin.service.FileService;
import com.aihub.common.web.dto.PageResult;
import com.aihub.common.web.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务实现
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {
    
    @Autowired
    private FileMapper fileMapper;
    
    @Autowired(required = false)
    private HttpServletRequest request;
    
    /**
     * 文件存储根目录
     */
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;
    
    /**
     * 获取文件存储的绝对路径
     */
    private String getAbsoluteUploadPath() {
        String path = uploadPath;
        // 如果是相对路径，转换为绝对路径
        if (path.startsWith("./") || (!path.startsWith("/") && !path.contains(":"))) {
            // 获取项目根目录或用户目录
            String baseDir = System.getProperty("user.dir");
            if (path.startsWith("./")) {
                path = path.substring(2);
            }
            path = baseDir + "/" + path;
        }
        return path;
    }
    
    /**
     * 文件访问URL前缀
     */
    @Value("${file.upload.url-prefix:/api/files}")
    private String urlPrefix;
    
    /**
     * 最大文件大小（字节），默认10MB
     */
    @Value("${file.upload.max-size:10485760}")
    private Long maxFileSize;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        if (request != null) {
            Object userId = request.getAttribute("userId");
            if (userId instanceof Long) {
                return (Long) userId;
            } else if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            }
        }
        return null;
    }
    
    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        if (request != null) {
            Object username = request.getAttribute("username");
            if (username instanceof String) {
                return (String) username;
            }
        }
        return null;
    }
    
    @Override
    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        // 验证文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        try {
            // 生成文件存储路径：category/yyyy/MM/dd/uuid.extension
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String datePath = LocalDate.now().format(DATE_FORMATTER);
            String storageFilename = uuid + extension;
            String relativePath = (category != null ? category + "/" : "") + datePath + "/" + storageFilename;
            String fileUrl = urlPrefix + "/" + relativePath;
            
            // 获取绝对路径
            String absoluteUploadPath = getAbsoluteUploadPath();
            Path fullPath = Paths.get(absoluteUploadPath, relativePath);
            
            // 确保父目录存在
            Path parentDir = fullPath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            
            // 保存文件
            file.transferTo(fullPath.toFile());
            
            // 保存文件元数据到数据库
            File fileEntity = new File();
            fileEntity.setFilename(originalFilename);
            fileEntity.setStorageFilename(storageFilename);
            fileEntity.setFilePath(relativePath);
            fileEntity.setFileUrl(fileUrl);
            fileEntity.setFileSize(file.getSize());
            fileEntity.setContentType(file.getContentType());
            fileEntity.setCategory(category != null ? category : "image");
            fileEntity.setUploadUserId(getCurrentUserId());
            fileEntity.setUploadUsername(getCurrentUsername());
            fileEntity.setReferenceCount(0);
            fileEntity.setStatus(1);
            fileEntity.setIsDeleted(0);
            
            fileMapper.insert(fileEntity);
            
            log.info("文件上传成功: id={}, category={}, filename={}, size={}, path={}", 
                fileEntity.getId(), category, originalFilename, file.getSize(), fullPath);
            
            // 构建响应
            FileUploadResponse response = new FileUploadResponse();
            response.setUrl(fileUrl);
            response.setFilename(originalFilename);
            response.setSize(file.getSize());
            response.setContentType(file.getContentType());
            
            return response;
        } catch (IOException e) {
            log.error("文件上传失败: category={}, filename={}", category, file.getOriginalFilename(), e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        try {
            // 从数据库查询文件信息
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<File> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            queryWrapper.eq(File::getFileUrl, fileUrl);
            queryWrapper.eq(File::getIsDeleted, 0);
            
            File fileEntity = fileMapper.selectOne(queryWrapper);
            if (fileEntity == null) {
                log.warn("文件不存在或已删除: url={}", fileUrl);
                return;
            }
            
            // 逻辑删除（标记为已删除）
            LambdaUpdateWrapper<File> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(File::getId, fileEntity.getId());
            updateWrapper.set(File::getIsDeleted, 1);
            fileMapper.update(null, updateWrapper);
            
            log.info("文件删除成功（逻辑删除）: id={}, url={}", fileEntity.getId(), fileUrl);
            
            // 可选：物理删除文件（谨慎使用）
            // Path fullPath = Paths.get(uploadPath, fileEntity.getFilePath());
            // java.io.File file = fullPath.toFile();
            // if (file.exists() && file.isFile()) {
            //     file.delete();
            // }
        } catch (Exception e) {
            log.error("文件删除失败: url={}", fileUrl, e);
            throw new BusinessException("文件删除失败: " + e.getMessage());
        }
    }
    
    @Override
    public PageResult<FileListResponse> getFileList(String category, String keyword, Long current, Integer size) {
        try {
            // 计算偏移量
            Long offset = (current - 1) * size;
            
            // 从数据库查询文件列表
            List<FileListResponse> files = fileMapper.selectFileList(category, keyword, offset, size);
            
            // 统计总数
            Long total = fileMapper.countFileList(category, keyword);
            
            // 计算总页数
            Long pages = (total + size - 1) / size;
            
            // 构建分页结果
            PageResult<FileListResponse> result = new PageResult<>();
            result.setRecords(files);
            result.setTotal(total);
            result.setCurrent(current.intValue());
            result.setSize(size);
            result.setPages(pages);
            
            return result;
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            throw new BusinessException("获取文件列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public byte[] downloadFile(String fileUrl) {
        try {
            // 从数据库查询文件信息
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<File> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            queryWrapper.eq(File::getFileUrl, fileUrl);
            queryWrapper.eq(File::getIsDeleted, 0);
            
            File fileEntity = fileMapper.selectOne(queryWrapper);
            if (fileEntity == null) {
                throw new BusinessException("文件不存在或已删除");
            }
            
            // 读取文件
            String absoluteUploadPath = getAbsoluteUploadPath();
            Path fullPath = Paths.get(absoluteUploadPath, fileEntity.getFilePath());
            java.io.File file = fullPath.toFile();
            
            if (!file.exists() || !file.isFile()) {
                throw new BusinessException("物理文件不存在");
            }
            
            return Files.readAllBytes(fullPath);
        } catch (IOException e) {
            log.error("文件下载失败: url={}", fileUrl, e);
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }
    
}
