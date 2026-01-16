package com.aihub.admin.mapper;

import com.aihub.admin.dto.response.DictTypeResponse;
import com.aihub.admin.entity.DictType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典类型Mapper接口
 */
public interface DictTypeMapper extends BaseMapper<DictType> {
    
    /**
     * 查询字典类型列表（分页、搜索、筛选）
     */
    List<DictTypeResponse> selectDictTypeList(
            @Param("dictName") String dictName,
            @Param("dictType") String dictType,
            @Param("status") Integer status,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("offset") Long offset,
            @Param("limit") Integer limit);
    
    /**
     * 统计字典类型总数
     */
    Long countDictTypeList(
            @Param("dictName") String dictName,
            @Param("dictType") String dictType,
            @Param("status") Integer status,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);
}
