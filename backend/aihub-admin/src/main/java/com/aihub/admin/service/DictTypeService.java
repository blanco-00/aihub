package com.aihub.admin.service;

import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.DictTypeListRequest;
import com.aihub.admin.dto.request.CreateDictTypeRequest;
import com.aihub.admin.dto.request.UpdateDictTypeRequest;
import com.aihub.admin.dto.response.DictTypeResponse;

/**
 * 字典类型服务接口
 */
public interface DictTypeService {
    
    /**
     * 获取字典类型列表（分页、搜索、筛选）
     */
    PageResult<DictTypeResponse> getDictTypeList(DictTypeListRequest request);
    
    /**
     * 根据ID获取字典类型详情
     */
    DictTypeResponse getDictTypeById(Long id);
    
    /**
     * 创建字典类型
     */
    void createDictType(CreateDictTypeRequest request);
    
    /**
     * 更新字典类型
     */
    void updateDictType(Long id, UpdateDictTypeRequest request);
    
    /**
     * 删除字典类型（逻辑删除）
     */
    void deleteDictType(Long id);
    
    /**
     * 刷新字典缓存
     */
    void refreshCache();
}
