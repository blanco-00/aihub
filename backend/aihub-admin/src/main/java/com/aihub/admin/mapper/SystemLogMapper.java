package com.aihub.admin.mapper;

import com.aihub.admin.dto.request.SystemLogListRequest;
import com.aihub.admin.dto.response.SystemLogResponse;
import com.aihub.admin.entity.SystemLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统日志Mapper接口
 */
@Mapper
public interface SystemLogMapper extends BaseMapper<SystemLog> {
    
    /**
     * 查询系统日志列表（分页、搜索、筛选）
     */
    List<SystemLogResponse> selectSystemLogList(@Param("request") SystemLogListRequest request, 
                                                @Param("offset") Long offset, 
                                                @Param("size") Integer size);
    
    /**
     * 统计系统日志总数
     */
    Long countSystemLogList(@Param("request") SystemLogListRequest request);
}
