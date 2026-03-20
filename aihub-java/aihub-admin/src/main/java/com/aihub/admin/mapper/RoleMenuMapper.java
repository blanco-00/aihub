package com.aihub.admin.mapper;

import com.aihub.admin.entity.RoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色菜单关联Mapper
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
    
    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据角色ID删除所有关联
     */
    void deleteByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 批量插入角色菜单关联
     */
    void batchInsert(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}
