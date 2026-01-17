package com.aihub.admin.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 发布通知请求DTO
 */
@Data
public class PublishNoticeRequest {
    
    // 发布范围（当 publishType 不为 1 时使用）
    private List<Long> departmentIds;
    
    private List<Long> roleIds;
    
    private List<Long> userIds;
}
