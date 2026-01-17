package com.aihub.admin.controller;

import com.aihub.admin.annotation.OperationLog;
import com.aihub.common.web.dto.Result;
import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.CreateDictDataRequest;
import com.aihub.admin.dto.request.UpdateDictDataRequest;
import com.aihub.admin.dto.response.DictDataResponse;
import com.aihub.admin.service.DictDataService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 字典数据管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/dict-data")
public class DictDataController {
    
    @Autowired
    private DictDataService dictDataService;
    
    /**
     * 根据字典类型获取字典数据列表（分页）
     */
    @GetMapping
    public Result<PageResult<DictDataResponse>> getDictDataList(
            @RequestParam String dictType,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        
        PageResult<DictDataResponse> result = dictDataService.getDictDataListByType(dictType, current, size);
        return Result.success(result);
    }
    
    /**
     * 根据ID获取字典数据详情
     */
    @GetMapping("/{id}")
    public Result<DictDataResponse> getDictDataById(@PathVariable Long id) {
        DictDataResponse dictData = dictDataService.getDictDataById(id);
        return Result.success(dictData);
    }
    
    /**
     * 创建字典数据
     */
    @OperationLog(module = "字典管理", operation = "创建字典数据", recordParams = true)
    @PostMapping
    public Result<Void> createDictData(@Valid @RequestBody CreateDictDataRequest request) {
        dictDataService.createDictData(request);
        return Result.success();
    }
    
    /**
     * 更新字典数据
     */
    @OperationLog(module = "字典管理", operation = "修改字典数据", recordParams = true)
    @PutMapping("/{id}")
    public Result<Void> updateDictData(@PathVariable Long id, 
                                      @Valid @RequestBody UpdateDictDataRequest request) {
        dictDataService.updateDictData(id, request);
        return Result.success();
    }
    
    /**
     * 删除字典数据（逻辑删除）
     */
    @OperationLog(module = "字典管理", operation = "删除字典数据")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDictData(@PathVariable Long id) {
        dictDataService.deleteDictData(id);
        return Result.success();
    }
}
