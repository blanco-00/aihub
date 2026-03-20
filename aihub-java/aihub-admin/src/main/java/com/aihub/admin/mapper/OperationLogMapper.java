package com.aihub.admin.mapper;

import com.aihub.admin.dto.request.OperationLogListRequest;
import com.aihub.admin.dto.response.OperationLogResponse;
import com.aihub.admin.entity.OperationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志Mapper接口
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    
    /**
     * 查询操作日志列表（分页、搜索、筛选）
     */
    List<OperationLogResponse> selectOperationLogList(@Param("request") OperationLogListRequest request, 
                                                       @Param("offset") Long offset, 
                                                       @Param("size") Integer size);
    
    /**
     * 统计操作日志总数
     */
    Long countOperationLogList(@Param("request") OperationLogListRequest request);
}
