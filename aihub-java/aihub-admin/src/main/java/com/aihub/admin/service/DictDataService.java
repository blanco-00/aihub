package com.aihub.admin.service;

import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.CreateDictDataRequest;
import com.aihub.admin.dto.request.UpdateDictDataRequest;
import com.aihub.admin.dto.response.DictDataResponse;

/**
 * 字典数据服务接口
 */
public interface DictDataService {
    
    /**
     * 根据字典类型获取字典数据列表（分页）
     */
    PageResult<DictDataResponse> getDictDataListByType(String dictType, Integer current, Integer size);
    
    /**
     * 根据ID获取字典数据详情
     */
    DictDataResponse getDictDataById(Long id);
    
    /**
     * 创建字典数据
     */
    void createDictData(CreateDictDataRequest request);
    
    /**
     * 更新字典数据
     */
    void updateDictData(Long id, UpdateDictDataRequest request);
    
    /**
     * 删除字典数据（逻辑删除）
     */
    void deleteDictData(Long id);
}
