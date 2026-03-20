package com.aihub.admin.mapper;

import com.aihub.admin.dto.response.DictDataResponse;
import com.aihub.admin.entity.DictData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据Mapper接口
 */
public interface DictDataMapper extends BaseMapper<DictData> {
    
    /**
     * 根据字典类型查询字典数据列表
     */
    List<DictDataResponse> selectDictDataListByType(
            @Param("dictType") String dictType,
            @Param("offset") Long offset,
            @Param("limit") Integer limit);
    
    /**
     * 统计字典数据总数
     */
    Long countDictDataListByType(@Param("dictType") String dictType);
}
