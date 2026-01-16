package com.aihub.admin.controller;

import com.aihub.common.web.dto.Result;
import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.DictTypeListRequest;
import com.aihub.admin.dto.request.CreateDictTypeRequest;
import com.aihub.admin.dto.request.UpdateDictTypeRequest;
import com.aihub.admin.dto.response.DictTypeResponse;
import com.aihub.admin.service.DictTypeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 字典类型管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/dict-types")
public class DictTypeController {
    
    @Autowired
    private DictTypeService dictTypeService;
    
    /**
     * 获取字典类型列表（分页、搜索、筛选）
     */
    @GetMapping
    public Result<PageResult<DictTypeResponse>> getDictTypeList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String dictName,
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        DictTypeListRequest request = new DictTypeListRequest();
        request.setCurrent(current);
        request.setSize(size);
        request.setDictName(dictName);
        request.setDictType(dictType);
        request.setStatus(status);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        
        PageResult<DictTypeResponse> result = dictTypeService.getDictTypeList(request);
        return Result.success(result);
    }
    
    /**
     * 根据ID获取字典类型详情
     */
    @GetMapping("/{id}")
    public Result<DictTypeResponse> getDictTypeById(@PathVariable Long id) {
        DictTypeResponse dictType = dictTypeService.getDictTypeById(id);
        return Result.success(dictType);
    }
    
    /**
     * 创建字典类型
     */
    @PostMapping
    public Result<Void> createDictType(@Valid @RequestBody CreateDictTypeRequest request) {
        dictTypeService.createDictType(request);
        return Result.success();
    }
    
    /**
     * 更新字典类型
     */
    @PutMapping("/{id}")
    public Result<Void> updateDictType(@PathVariable Long id, 
                                       @Valid @RequestBody UpdateDictTypeRequest request) {
        dictTypeService.updateDictType(id, request);
        return Result.success();
    }
    
    /**
     * 删除字典类型（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteDictType(@PathVariable Long id) {
        dictTypeService.deleteDictType(id);
        return Result.success();
    }
    
    /**
     * 刷新字典缓存
     */
    @PostMapping("/refresh-cache")
    public Result<Void> refreshCache() {
        dictTypeService.refreshCache();
        return Result.success();
    }
}
