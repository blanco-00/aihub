package com.aihub.admin.controller;

import com.aihub.admin.annotation.OperationLog;
import com.aihub.common.web.dto.Result;
import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.NoticeCategoryListRequest;
import com.aihub.admin.dto.request.CreateNoticeCategoryRequest;
import com.aihub.admin.dto.request.UpdateNoticeCategoryRequest;
import com.aihub.admin.dto.response.NoticeCategoryResponse;
import com.aihub.admin.service.NoticeCategoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通知分类管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notice-categories")
public class NoticeCategoryController {
    
    @Autowired
    private NoticeCategoryService noticeCategoryService;
    
    /**
     * 获取通知分类列表（分页、搜索、筛选）
     */
    @GetMapping
    public Result<PageResult<NoticeCategoryResponse>> getNoticeCategoryList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Integer status) {
        
        NoticeCategoryListRequest request = new NoticeCategoryListRequest();
        request.setCurrent(current);
        request.setSize(size);
        request.setName(name);
        request.setCode(code);
        request.setStatus(status);
        
        PageResult<NoticeCategoryResponse> result = noticeCategoryService.getNoticeCategoryList(request);
        return Result.success(result);
    }
    
    /**
     * 根据ID获取通知分类详情
     */
    @GetMapping("/{id}")
    public Result<NoticeCategoryResponse> getNoticeCategoryById(@PathVariable Long id) {
        NoticeCategoryResponse result = noticeCategoryService.getNoticeCategoryById(id);
        return Result.success(result);
    }
    
    /**
     * 创建通知分类
     */
    @OperationLog(module = "通知管理", operation = "创建通知分类", recordParams = true)
    @PostMapping
    public Result<Void> createNoticeCategory(@Valid @RequestBody CreateNoticeCategoryRequest request) {
        noticeCategoryService.createNoticeCategory(request);
        return Result.success();
    }
    
    /**
     * 更新通知分类
     */
    @OperationLog(module = "通知管理", operation = "修改通知分类", recordParams = true)
    @PutMapping("/{id}")
    public Result<Void> updateNoticeCategory(@PathVariable Long id, 
                                             @Valid @RequestBody UpdateNoticeCategoryRequest request) {
        noticeCategoryService.updateNoticeCategory(id, request);
        return Result.success();
    }
    
    /**
     * 删除通知分类（逻辑删除）
     */
    @OperationLog(module = "通知管理", operation = "删除通知分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNoticeCategory(@PathVariable Long id) {
        noticeCategoryService.deleteNoticeCategory(id);
        return Result.success();
    }
}
