package com.aihub.mapper;

import com.aihub.dto.UserListResponse;
import com.aihub.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
    
    /**
     * 查询用户列表（支持搜索和筛选）
     */
    List<UserListResponse> selectUserList(@Param("keyword") String keyword, 
                                          @Param("phone") String phone,
                                          @Param("role") String role, 
                                          @Param("status") Integer status,
                                          @Param("offset") Long offset,
                                          @Param("limit") Integer limit);
    
    /**
     * 统计用户总数（支持搜索和筛选）
     */
    Long countUserList(@Param("keyword") String keyword, 
                      @Param("phone") String phone,
                      @Param("role") String role, 
                      @Param("status") Integer status);
}
