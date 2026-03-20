package com.aihub.admin.mapper;

import com.aihub.admin.dto.request.LoginLogListRequest;
import com.aihub.admin.dto.response.LoginLogResponse;
import com.aihub.admin.entity.LoginLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录日志Mapper接口
 */
public interface LoginLogMapper extends BaseMapper<LoginLog> {
    
    /**
     * 查询登录日志列表（分页、搜索、筛选）
     */
    List<LoginLogResponse> selectLoginLogList(@Param("request") LoginLogListRequest request, 
                                               @Param("offset") Long offset, 
                                               @Param("size") Integer size);
    
    /**
     * 统计登录日志总数
     */
    Long countLoginLogList(@Param("request") LoginLogListRequest request);
}
