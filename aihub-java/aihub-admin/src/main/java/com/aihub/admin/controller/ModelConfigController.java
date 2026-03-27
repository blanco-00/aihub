package com.aihub.admin.controller;

import com.aihub.admin.annotation.OperationLog;
import com.aihub.common.web.dto.Result;
import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.CreateModelConfigRequest;
import com.aihub.admin.dto.request.UpdateModelConfigRequest;
import com.aihub.admin.dto.response.ModelConfigResponse;
import com.aihub.admin.service.ModelConfigService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/model-configs")
public class ModelConfigController {

    @Autowired
    private ModelConfigService modelConfigService;

    @GetMapping
    public Result<PageResult<ModelConfigResponse>> getModelConfigList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String vendor,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String modelType) {

        PageResult<ModelConfigResponse> result = modelConfigService.getModelConfigList(keyword, vendor, status, modelType, current, size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ModelConfigResponse> getModelConfigById(@PathVariable Long id) {
        ModelConfigResponse modelConfig = modelConfigService.getModelConfigById(id);
        return Result.success(modelConfig);
    }

    @GetMapping("/enabled")
    public Result<List<ModelConfigResponse>> getEnabledModelConfigs() {
        List<ModelConfigResponse> modelConfigs = modelConfigService.getEnabledModelConfigs();
        return Result.success(modelConfigs);
    }

    @OperationLog(module = "模型管理", operation = "创建模型", recordParams = true)
    @PostMapping
    public Result<Void> createModelConfig(@Valid @RequestBody CreateModelConfigRequest request) {
        modelConfigService.createModelConfig(request);
        return Result.success();
    }

    @OperationLog(module = "模型管理", operation = "修改模型", recordParams = true)
    @PutMapping("/{id}")
    public Result<Void> updateModelConfig(@PathVariable Long id, @Valid @RequestBody UpdateModelConfigRequest request) {
        modelConfigService.updateModelConfig(id, request);
        return Result.success();
    }

    @OperationLog(module = "模型管理", operation = "删除模型")
    @DeleteMapping("/{id}")
    public Result<Void> deleteModelConfig(@PathVariable Long id) {
        modelConfigService.deleteModelConfig(id);
        return Result.success();
    }

    @OperationLog(module = "模型管理", operation = "启用/禁用模型", recordParams = true)
    @PutMapping("/{id}/status")
    public Result<Void> toggleModelConfigStatus(@PathVariable Long id, @RequestParam Integer status) {
        modelConfigService.toggleModelConfigStatus(id, status);
        return Result.success();
    }

    @OperationLog(module = "模型管理", operation = "设置默认模型", recordParams = true)
    @PutMapping("/{id}/default")
    public Result<Void> setDefaultModel(@PathVariable Long id) {
        modelConfigService.setDefaultModel(id);
        return Result.success();
    }

    @GetMapping("/default")
    public Result<ModelConfigResponse> getDefaultModel() {
        ModelConfigResponse modelConfig = modelConfigService.getDefaultModel();
        return Result.success(modelConfig);
    }
}
