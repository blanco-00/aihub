package com.aihub.admin.service;

import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.NoticeListRequest;
import com.aihub.admin.dto.request.CreateNoticeRequest;
import com.aihub.admin.dto.request.UpdateNoticeRequest;
import com.aihub.admin.dto.request.PublishNoticeRequest;
import com.aihub.admin.dto.response.NoticeResponse;
import com.aihub.admin.dto.response.NoticeListResponse;
import com.aihub.admin.dto.response.NoticeDetailResponse;
import com.aihub.admin.dto.response.MyNoticeResponse;

/**
 * 通知公告服务接口
 */
public interface NoticeService {
    
    /**
     * 获取通知列表（分页、搜索、筛选，管理员使用）
     */
    PageResult<NoticeListResponse> getNoticeList(NoticeListRequest request);
    
    /**
     * 根据ID获取通知详情（管理员使用）
     */
    NoticeDetailResponse getNoticeById(Long id);
    
    /**
     * 创建通知（草稿状态）
     */
    void createNotice(CreateNoticeRequest request, Long publisherId, String publisherName);
    
    /**
     * 更新通知
     */
    void updateNotice(Long id, UpdateNoticeRequest request);
    
    /**
     * 发布通知（计算接收用户并创建 notice_user 记录）
     */
    void publishNotice(Long id, PublishNoticeRequest request);
    
    /**
     * 撤回通知
     */
    void withdrawNotice(Long id);
    
    /**
     * 删除通知（逻辑删除）
     */
    void deleteNotice(Long id);
    
    /**
     * 获取我的通知列表（分页，用户端使用）
     */
    PageResult<MyNoticeResponse> getMyNotices(Long userId, Integer current, Integer size, Integer isRead);
    
    /**
     * 获取未读通知数量（用户端使用）
     */
    Long getUnreadCount(Long userId);
    
    /**
     * 查看通知详情（自动标记已读，用户端使用）
     */
    NoticeDetailResponse getNoticeDetail(Long noticeId, Long userId);
    
    /**
     * 标记通知为已读
     */
    void markAsRead(Long noticeId, Long userId);
    
    /**
     * 全部标记为已读
     */
    void markAllAsRead(Long userId);
}
