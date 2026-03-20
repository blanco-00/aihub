package com.aihub.admin.service;

import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.NoticeCategoryListRequest;
import com.aihub.admin.dto.request.CreateNoticeCategoryRequest;
import com.aihub.admin.dto.request.UpdateNoticeCategoryRequest;
import com.aihub.admin.dto.response.NoticeCategoryResponse;

/**
 * 通知分类服务接口
 */
public interface NoticeCategoryService {
    
    /**
     * 获取通知分类列表（分页、搜索、筛选）
     */
    PageResult<NoticeCategoryResponse> getNoticeCategoryList(NoticeCategoryListRequest request);
    
    /**
     * 根据ID获取通知分类详情
     */
    NoticeCategoryResponse getNoticeCategoryById(Long id);
    
    /**
     * 创建通知分类
     */
    void createNoticeCategory(CreateNoticeCategoryRequest request);
    
    /**
     * 更新通知分类
     */
    void updateNoticeCategory(Long id, UpdateNoticeCategoryRequest request);
    
    /**
     * 删除通知分类（逻辑删除）
     */
    void deleteNoticeCategory(Long id);
}
