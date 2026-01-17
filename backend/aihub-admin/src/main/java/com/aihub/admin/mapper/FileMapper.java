package com.aihub.admin.mapper;

import com.aihub.admin.dto.response.FileListResponse;
import com.aihub.admin.entity.File;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件Mapper接口
 */
public interface FileMapper extends BaseMapper<File> {
    
    /**
     * 查询文件列表（分页、搜索、筛选）
     */
    List<FileListResponse> selectFileList(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("offset") Long offset,
            @Param("limit") Integer limit);
    
    /**
     * 统计文件总数
     */
    Long countFileList(
            @Param("category") String category,
            @Param("keyword") String keyword);
}
