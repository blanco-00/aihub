package com.aihub.admin.service.impl;

import com.aihub.admin.dto.response.ModelTestResult;
import com.aihub.admin.entity.ModelTestHistory;
import com.aihub.admin.mapper.ModelTestHistoryMapper;
import com.aihub.admin.service.ModelTestHistoryService;
import com.aihub.common.web.dto.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ModelTestHistoryServiceImpl implements ModelTestHistoryService {

    @Autowired
    private ModelTestHistoryMapper modelTestHistoryMapper;

    @Override
    public PageResult<ModelTestResult> getTestHistoryList(Long modelId, String keyword, Long current, Long size) {
        long startTime = System.currentTimeMillis();

        Long offset = (current - 1) * size;

        List<ModelTestHistory> histories = modelTestHistoryMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ModelTestHistory>()
                .eq(modelId != null, ModelTestHistory::getModelId, modelId)
                .and(wrapper -> {
                    if (keyword != null && !keyword.isEmpty()) {
                        wrapper.like(ModelTestHistory::getUserMessage, keyword);
                        wrapper.or().like(ModelTestHistory::getModelResponse, keyword);
                    }
                })
                .orderByDesc(ModelTestHistory::getCreatedAt)
                .last("LIMIT " + offset + ", " + size)
        );

        Long total = modelTestHistoryMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ModelTestHistory>()
                .eq(modelId != null, ModelTestHistory::getModelId, modelId)
                .and(wrapper -> {
                    if (keyword != null && !keyword.isEmpty()) {
                        wrapper.like(ModelTestHistory::getUserMessage, keyword);
                        wrapper.or().like(ModelTestHistory::getModelResponse, keyword);
                    }
                })
        );

        Long pages = (total + size - 1) / size;

        List<ModelTestResult> results = histories.stream()
            .map(this::convertToResult)
            .collect(Collectors.toList());

        PageResult<ModelTestResult> result = new PageResult<>();
        result.setRecords(results);
        result.setTotal(total);
        result.setCurrent(current.intValue());
        result.setSize(size.intValue());
        result.setPages(pages);

        long totalTime = System.currentTimeMillis() - startTime;
        if (totalTime > 500) {
            log.warn("[性能警告] 测试历史列表查询总耗时: {}ms", totalTime);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTestHistory(ModelTestResult result) {
        ModelTestHistory history = convertToHistory(result);
        modelTestHistoryMapper.insert(history);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchTestHistory(List<ModelTestResult> results, Long userId) {
        for (ModelTestResult result : results) {
            ModelTestHistory history = convertToHistory(result);
            history.setUserId(userId);
            modelTestHistoryMapper.insert(history);
        }
    }

    private ModelTestResult convertToResult(ModelTestHistory history) {
        ModelTestResult result = new ModelTestResult();
        BeanUtils.copyProperties(history, result);
        result.setModelId(history.getModelId());
        result.setModelName(history.getModelName());
        result.setVendor(history.getVendor());
        result.setResponse(history.getModelResponse());
        result.setSuccess(history.getSuccess());
        result.setError(history.getErrorMessage());
        result.setResponseTimeMs(history.getResponseTimeMs());
        result.setTimestamp(history.getCreatedAt());
        return result;
    }

    private ModelTestHistory convertToHistory(ModelTestResult result) {
        ModelTestHistory history = new ModelTestHistory();
        history.setModelId(result.getModelId());
        history.setModelName(result.getModelName());
        history.setVendor(result.getVendor());
        history.setModelDisplayId("");
        history.setUserMessage(result.getUserMessage());
        history.setModelResponse(result.getResponse());
        history.setSuccess(result.getSuccess());
        history.setErrorMessage(result.getError());
        history.setResponseTimeMs(result.getResponseTimeMs());
        history.setTokenUsage(0);
        return history;
    }
}
