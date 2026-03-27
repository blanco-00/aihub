package com.aihub.admin.service;

import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.CreateModelConfigRequest;
import com.aihub.admin.dto.request.UpdateModelConfigRequest;
import com.aihub.admin.dto.response.ModelConfigResponse;

import java.util.List;

public interface ModelConfigService {

    PageResult<ModelConfigResponse> getModelConfigList(String keyword, String vendor, Integer status, String modelType, Long current, Long size);

    ModelConfigResponse getModelConfigById(Long id);

    void createModelConfig(CreateModelConfigRequest request);

    void updateModelConfig(Long id, UpdateModelConfigRequest request);

    void deleteModelConfig(Long id);

    void toggleModelConfigStatus(Long id, Integer status);

    void setDefaultModel(Long id);

    ModelConfigResponse getDefaultModel();

    List<ModelConfigResponse> getEnabledModelConfigs();
}
