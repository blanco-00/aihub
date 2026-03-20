package com.aihub.admin.mapper;

import com.aihub.admin.entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    /**
     * 根据用户ID查询角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID删除所有关联
     */
    void deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 批量插入用户角色关联
     */
    void batchInsert(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}
