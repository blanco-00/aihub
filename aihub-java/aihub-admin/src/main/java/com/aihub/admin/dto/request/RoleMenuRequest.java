package com.aihub.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色菜单关联请求DTO
 */
@Data
public class RoleMenuRequest {
    
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
    
    @NotNull(message = "菜单ID列表不能为空")
    private List<Long> menuIds;
}
