package com.aihub.admin.service.impl;

import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.NoticeCategoryListRequest;
import com.aihub.admin.dto.request.CreateNoticeCategoryRequest;
import com.aihub.admin.dto.request.UpdateNoticeCategoryRequest;
import com.aihub.admin.dto.response.NoticeCategoryResponse;
import com.aihub.admin.entity.NoticeCategory;
import com.aihub.common.web.exception.BusinessException;
import com.aihub.admin.mapper.NoticeCategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知分类服务实现
 */
@Slf4j
@Service
public class NoticeCategoryServiceImpl implements com.aihub.admin.service.NoticeCategoryService {
    
    @Autowired
    private NoticeCategoryMapper noticeCategoryMapper;
    
    @Override
    public PageResult<NoticeCategoryResponse> getNoticeCategoryList(NoticeCategoryListRequest request) {
        long startTime = System.currentTimeMillis();
        
        // 计算偏移量
        Long offset = (long) (request.getCurrent() - 1) * request.getSize();
        
        // 查询通知分类列表
        List<NoticeCategoryResponse> categories = noticeCategoryMapper.selectNoticeCategoryList(
            request, offset, request.getSize());
        
        // 统计总数
        Long total = noticeCategoryMapper.countNoticeCategoryList(request);
        
        // 计算总页数
        Long pages = (total + request.getSize() - 1) / request.getSize();
        
        // 构建分页结果
        PageResult<NoticeCategoryResponse> result = new PageResult<>();
        result.setRecords(categories);
        result.setTotal(total);
        result.setCurrent(request.getCurrent());
        result.setSize(request.getSize());
        result.setPages(pages);
        
        long duration = System.currentTimeMillis() - startTime;
        if (duration > 500) {
            log.warn("查询通知分类列表耗时较长: duration={}ms, name={}, code={}", 
                duration, request.getName(), request.getCode());
        }
        
        return result;
    }
    
    @Override
    public NoticeCategoryResponse getNoticeCategoryById(Long id) {
        NoticeCategory category = noticeCategoryMapper.selectById(id);
        if (category == null || category.getIsDeleted() == 1) {
            throw new BusinessException("通知分类不存在");
        }
        
        NoticeCategoryResponse response = new NoticeCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setCode(category.getCode());
        response.setDescription(category.getDescription());
        response.setSortOrder(category.getSortOrder());
        response.setStatus(category.getStatus());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        
        return response;
    }
    
    @Override
    @Transactional
    public void createNoticeCategory(CreateNoticeCategoryRequest request) {
        // 检查分类代码是否已存在
        NoticeCategory existing = noticeCategoryMapper.selectOne(
            new LambdaQueryWrapper<NoticeCategory>()
                .eq(NoticeCategory::getCode, request.getCode())
                .eq(NoticeCategory::getIsDeleted, 0)
        );
        
        if (existing != null) {
            log.warn("创建通知分类失败：分类代码已存在: code={}", request.getCode());
            throw new BusinessException("分类代码已存在");
        }
        
        // 创建通知分类
        NoticeCategory category = new NoticeCategory();
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        category.setIsDeleted(0);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        
        noticeCategoryMapper.insert(category);
        log.info("创建通知分类成功: id={}, name={}, code={}", 
            category.getId(), category.getName(), category.getCode());
    }
    
    @Override
    @Transactional
    public void updateNoticeCategory(Long id, UpdateNoticeCategoryRequest request) {
        // 查询通知分类
        NoticeCategory category = noticeCategoryMapper.selectById(id);
        if (category == null || category.getIsDeleted() == 1) {
            throw new BusinessException("通知分类不存在");
        }
        
        // 如果分类代码有变化，检查新代码是否已被使用
        if (!category.getCode().equals(request.getCode())) {
            NoticeCategory existing = noticeCategoryMapper.selectOne(
                new LambdaQueryWrapper<NoticeCategory>()
                    .eq(NoticeCategory::getCode, request.getCode())
                    .eq(NoticeCategory::getIsDeleted, 0)
                    .ne(NoticeCategory::getId, id)
            );
            
            if (existing != null) {
                log.warn("更新通知分类失败：分类代码已存在: code={}, id={}", request.getCode(), id);
                throw new BusinessException("分类代码已存在");
            }
        }
        
        // 更新通知分类
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setDescription(request.getDescription());
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }
        category.setUpdatedAt(LocalDateTime.now());
        
        noticeCategoryMapper.updateById(category);
        log.info("更新通知分类成功: id={}, name={}, code={}", 
            id, category.getName(), category.getCode());
    }
    
    @Override
    @Transactional
    public void deleteNoticeCategory(Long id) {
        // 查询通知分类
        NoticeCategory category = noticeCategoryMapper.selectById(id);
        if (category == null || category.getIsDeleted() == 1) {
            throw new BusinessException("通知分类不存在");
        }
        
        // 逻辑删除
        category.setIsDeleted(1);
        category.setUpdatedAt(LocalDateTime.now());
        noticeCategoryMapper.updateById(category);
        
        log.info("删除通知分类成功: id={}, name={}, code={}", 
            id, category.getName(), category.getCode());
    }
}
