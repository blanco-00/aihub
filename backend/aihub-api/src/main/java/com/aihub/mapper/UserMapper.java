package com.aihub.mapper;

import com.aihub.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     */
    User findByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查询用户
     */
    User findByEmail(@Param("email") String email);
    
    /**
     * 统计指定角色且未删除的用户数量
     */
    Long countByRoleAndNotDeleted(@Param("role") String role);
}
