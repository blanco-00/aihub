package com.aihub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色菜单关联实体
 */
@Data
@TableName("role_menu")
public class RoleMenu {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long roleId;
    
    private Long menuId;
    
    private LocalDateTime createdAt;
}
