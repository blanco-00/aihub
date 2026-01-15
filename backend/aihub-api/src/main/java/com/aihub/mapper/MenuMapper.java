package com.aihub.mapper;

import com.aihub.entity.Menu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单Mapper接口
 */
public interface MenuMapper extends BaseMapper<Menu> {
    
    /**
     * 根据父菜单ID查询子菜单列表
     */
    List<Menu> selectByParentId(@Param("parentId") Long parentId);
    
    /**
     * 查询所有菜单（树形结构）
     */
    List<Menu> selectAllMenus();
    
    /**
     * 根据角色ID查询菜单列表
     */
    List<Menu> selectByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据角色代码查询菜单列表
     */
    List<Menu> selectByRoleCode(@Param("roleCode") String roleCode);
    
    /**
     * 检查是否存在子菜单
     */
    Long countChildren(@Param("parentId") Long parentId);
}
