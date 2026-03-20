package com.aihub.admin.dto.request;

import lombok.Data;

/**
 * 登录日志列表查询请求DTO
 */
@Data
public class LoginLogListRequest {
    
    private Integer current = 1;
    
    private Integer size = 10;
    
    private Long userId;
    
    private String username;
    
    private String ip;
    
    private Integer status;
    
    private String startTime;
    
    private String endTime;
}
