package com.aihub.dto;

import lombok.Data;

/**
 * 用户列表查询请求DTO
 */
@Data
public class UserListRequest {
    
    /**
     * 当前页码（从1开始）
     */
    private Integer current = 1;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
    
    /**
     * 搜索关键词（用户名、昵称或邮箱）
     */
    private String keyword;

    /**
     * 手机号筛选
     */
    private String phone;
    
    /**
     * 角色筛选
     */
    private String role;
    
    /**
     * 状态筛选（1-启用，0-禁用）
     */
    private Integer status;
}
