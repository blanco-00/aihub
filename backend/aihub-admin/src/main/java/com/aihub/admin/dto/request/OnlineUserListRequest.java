package com.aihub.admin.dto.request;

import lombok.Data;

/**
 * 在线用户列表查询请求DTO
 */
@Data
public class OnlineUserListRequest {
    
    private Integer current = 1;
    
    private Integer size = 10;
    
    private String username;
}
