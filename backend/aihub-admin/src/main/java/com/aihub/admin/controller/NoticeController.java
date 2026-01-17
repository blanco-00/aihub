package com.aihub.admin.controller;

import com.aihub.admin.annotation.OperationLog;
import com.aihub.common.web.dto.Result;
import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.NoticeListRequest;
import com.aihub.admin.dto.request.CreateNoticeRequest;
import com.aihub.admin.dto.request.UpdateNoticeRequest;
import com.aihub.admin.dto.request.PublishNoticeRequest;
import com.aihub.admin.dto.response.NoticeListResponse;
import com.aihub.admin.dto.response.NoticeDetailResponse;
import com.aihub.admin.dto.response.MyNoticeResponse;
import com.aihub.admin.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通知公告管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notices")
public class NoticeController {
    
    @Autowired
    private NoticeService noticeService;
    
    /**
     * 获取通知列表（分页、搜索、筛选，管理员使用）
     */
    @GetMapping
    public Result<PageResult<NoticeListResponse>> getNoticeList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer publishType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        NoticeListRequest request = new NoticeListRequest();
        request.setCurrent(current);
        request.setSize(size);
        request.setTitle(title);
        request.setCategoryId(categoryId);
        request.setType(type);
        request.setPublishType(publishType);
        request.setStatus(status);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        
        PageResult<NoticeListResponse> result = noticeService.getNoticeList(request);
        return Result.success(result);
    }
    
    /**
     * 根据ID获取通知详情（管理员使用）
     */
    @GetMapping("/{id}")
    public Result<NoticeDetailResponse> getNoticeById(@PathVariable Long id) {
        NoticeDetailResponse result = noticeService.getNoticeById(id);
        return Result.success(result);
    }
    
    /**
     * 创建通知（草稿）
     */
    @OperationLog(module = "通知管理", operation = "创建通知", recordParams = true)
    @PostMapping
    public Result<Void> createNotice(@Valid @RequestBody CreateNoticeRequest request,
                                     HttpServletRequest httpRequest) {
        Long publisherId = (Long) httpRequest.getAttribute("userId");
        String publisherName = (String) httpRequest.getAttribute("username");
        noticeService.createNotice(request, publisherId, publisherName);
        return Result.success();
    }
    
    /**
     * 更新通知
     */
    @OperationLog(module = "通知管理", operation = "修改通知", recordParams = true)
    @PutMapping("/{id}")
    public Result<Void> updateNotice(@PathVariable Long id, 
                                     @Valid @RequestBody UpdateNoticeRequest request) {
        noticeService.updateNotice(id, request);
        return Result.success();
    }
    
    /**
     * 发布通知
     */
    @OperationLog(module = "通知管理", operation = "发布通知", recordParams = true)
    @PostMapping("/{id}/publish")
    public Result<Void> publishNotice(@PathVariable Long id, 
                                      @RequestBody(required = false) PublishNoticeRequest request) {
        noticeService.publishNotice(id, request);
        return Result.success();
    }
    
    /**
     * 撤回通知
     */
    @OperationLog(module = "通知管理", operation = "撤回通知")
    @PostMapping("/{id}/withdraw")
    public Result<Void> withdrawNotice(@PathVariable Long id) {
        noticeService.withdrawNotice(id);
        return Result.success();
    }
    
    /**
     * 删除通知（逻辑删除）
     */
    @OperationLog(module = "通知管理", operation = "删除通知")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return Result.success();
    }
    
    /**
     * 获取我的通知列表（分页，用户端使用）
     */
    @GetMapping("/my")
    public Result<PageResult<MyNoticeResponse>> getMyNotices(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer isRead,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PageResult<MyNoticeResponse> result = noticeService.getMyNotices(userId, current, size, isRead);
        return Result.success(result);
    }
    
    /**
     * 获取未读通知数量（用户端使用）
     */
    @GetMapping("/my/unread-count")
    public Result<Long> getUnreadCount(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Long count = noticeService.getUnreadCount(userId);
        return Result.success(count);
    }
    
    /**
     * 查看通知详情（自动标记已读，用户端使用）
     */
    @GetMapping("/{id}/detail")
    public Result<NoticeDetailResponse> getNoticeDetail(@PathVariable Long id,
                                                        HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        NoticeDetailResponse result = noticeService.getNoticeDetail(id, userId);
        return Result.success(result);
    }
    
    /**
     * 标记通知为已读（用户端使用）
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id,
                                   HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        noticeService.markAsRead(id, userId);
        return Result.success();
    }
    
    /**
     * 全部标记为已读（用户端使用）
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        noticeService.markAllAsRead(userId);
        return Result.success();
    }
}
