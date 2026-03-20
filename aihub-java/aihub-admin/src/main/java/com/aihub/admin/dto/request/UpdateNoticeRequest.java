package com.aihub.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新通知请求DTO
 */
@Data
public class UpdateNoticeRequest {
    
    @NotBlank(message = "通知标题不能为空")
    @Size(max = 200, message = "通知标题长度不能超过200个字符")
    private String title;
    
    @NotBlank(message = "通知内容不能为空")
    private String content;
    
    private Long categoryId;
    
    private Integer type;
    
    private Integer publishType;
    
    private LocalDateTime expireTime;
    
    private Integer sortOrder;
    
    // 发布范围（当 publishType 不为 1 时使用）
    private List<Long> departmentIds;
    
    private List<Long> roleIds;
    
    private List<Long> userIds;
}
