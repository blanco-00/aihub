package com.aihub.admin.service.impl;

import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.CreateModelConfigRequest;
import com.aihub.admin.dto.request.UpdateModelConfigRequest;
import com.aihub.admin.dto.response.ModelConfigResponse;
import com.aihub.admin.entity.ModelConfig;
import com.aihub.admin.mapper.ModelConfigMapper;
import com.aihub.admin.service.ModelConfigService;
import com.aihub.common.web.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ModelConfigServiceImpl implements ModelConfigService {

    @Autowired
    private ModelConfigMapper modelConfigMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public PageResult<ModelConfigResponse> getModelConfigList(String keyword, String vendor, Integer status, Long current, Long size) {
        long startTime = System.currentTimeMillis();

        Long offset = (current - 1) * size;

        List<ModelConfig> modelConfigs = modelConfigMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ModelConfig>()
                .like(keyword != null && !keyword.isEmpty(), ModelConfig::getName, keyword)
                .eq(vendor != null && !vendor.isEmpty(), ModelConfig::getVendor, vendor)
                .eq(status != null, ModelConfig::getStatus, status)
                .eq(ModelConfig::getIsDeleted, 0)
                .orderByDesc(ModelConfig::getCreatedAt)
                .last("LIMIT " + offset + ", " + size)
        );

        Long total = modelConfigMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ModelConfig>()
                .like(keyword != null && !keyword.isEmpty(), ModelConfig::getName, keyword)
                .eq(vendor != null && !vendor.isEmpty(), ModelConfig::getVendor, vendor)
                .eq(status != null, ModelConfig::getStatus, status)
                .eq(ModelConfig::getIsDeleted, 0)
        );

        Long pages = (total + size - 1) / size;

        List<ModelConfigResponse> responses = modelConfigs.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());

        PageResult<ModelConfigResponse> result = new PageResult<>();
        result.setRecords(responses);
        result.setTotal(total);
        result.setCurrent(current.intValue());
        result.setSize(size.intValue());
        result.setPages(pages);

        long totalTime = System.currentTimeMillis() - startTime;
        if (totalTime > 500) {
            log.warn("[性能警告] 模型配置列表查询总耗时: {}ms", totalTime);
        }

        return result;
    }

    @Override
    public ModelConfigResponse getModelConfigById(Long id) {
        ModelConfig modelConfig = modelConfigMapper.selectById(id);
        if (modelConfig == null || modelConfig.getIsDeleted() == 1) {
            throw new BusinessException("模型配置不存在");
        }
        return convertToResponse(modelConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createModelConfig(CreateModelConfigRequest request) {
        ModelConfig existing = modelConfigMapper.findByVendorAndModelId(request.getVendor(), request.getModelId());
        if (existing != null && existing.getIsDeleted() == 0) {
            throw new BusinessException("该厂商和模型ID的组合已存在");
        }

        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setName(request.getName());
        modelConfig.setVendor(request.getVendor());
        modelConfig.setModelId(request.getModelId());
        modelConfig.setApiKey(passwordEncoder.encode(request.getApiKey()));
        modelConfig.setBaseUrl(request.getBaseUrl());
        modelConfig.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        modelConfig.setConfig(request.getConfig());
        modelConfig.setCreatedAt(LocalDateTime.now());
        modelConfig.setUpdatedAt(LocalDateTime.now());
        modelConfig.setIsDeleted(0);

        modelConfigMapper.insert(modelConfig);
        log.info("创建模型配置: vendor={}, modelId={}", request.getVendor(), request.getModelId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateModelConfig(Long id, UpdateModelConfigRequest request) {
        ModelConfig modelConfig = modelConfigMapper.selectById(id);
        if (modelConfig == null || modelConfig.getIsDeleted() == 1) {
            throw new BusinessException("模型配置不存在");
        }

        ModelConfig existing = modelConfigMapper.findByVendorAndModelId(request.getVendor(), request.getModelId());
        if (existing != null && !existing.getId().equals(id) && existing.getIsDeleted() == 0) {
            throw new BusinessException("该厂商和模型ID的组合已存在");
        }

        modelConfig.setName(request.getName());
        modelConfig.setVendor(request.getVendor());
        modelConfig.setModelId(request.getModelId());
        modelConfig.setApiKey(passwordEncoder.encode(request.getApiKey()));
        modelConfig.setBaseUrl(request.getBaseUrl());
        modelConfig.setStatus(request.getStatus());
        modelConfig.setConfig(request.getConfig());
        modelConfig.setUpdatedAt(LocalDateTime.now());

        modelConfigMapper.updateById(modelConfig);
        log.info("更新模型配置: id={}, vendor={}, modelId={}", id, request.getVendor(), request.getModelId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModelConfig(Long id) {
        ModelConfig modelConfig = modelConfigMapper.selectById(id);
        if (modelConfig == null || modelConfig.getIsDeleted() == 1) {
            throw new BusinessException("模型配置不存在");
        }

        modelConfig.setIsDeleted(1);
        modelConfig.setUpdatedAt(LocalDateTime.now());
        modelConfigMapper.updateById(modelConfig);
        log.info("删除模型配置: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleModelConfigStatus(Long id, Integer status) {
        ModelConfig modelConfig = modelConfigMapper.selectById(id);
        if (modelConfig == null || modelConfig.getIsDeleted() == 1) {
            throw new BusinessException("模型配置不存在");
        }

        modelConfig.setStatus(status);
        modelConfig.setUpdatedAt(LocalDateTime.now());
        modelConfigMapper.updateById(modelConfig);
        log.info("切换模型配置状态: id={}, status={}", id, status);
    }

    @Override
    public List<ModelConfigResponse> getEnabledModelConfigs() {
        List<ModelConfig> modelConfigs = modelConfigMapper.selectEnabledModels();
        return modelConfigs.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    private ModelConfigResponse convertToResponse(ModelConfig modelConfig) {
        ModelConfigResponse response = new ModelConfigResponse();
        BeanUtils.copyProperties(modelConfig, response);
        response.setApiKey("******");
        return response;
    }
}
