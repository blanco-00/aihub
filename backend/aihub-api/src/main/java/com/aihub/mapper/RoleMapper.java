package com.aihub.mapper;

import com.aihub.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色Mapper接口
 */
public interface RoleMapper extends BaseMapper<Role> {
    
    /**
     * 根据角色代码查询角色
     */
    Role selectByCode(@Param("code") String code);
    
    /**
     * 查询所有启用的角色
     */
    List<Role> selectAllEnabled();
}
