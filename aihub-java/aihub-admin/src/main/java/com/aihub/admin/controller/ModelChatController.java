package com.aihub.admin.controller;

import com.aihub.admin.dto.request.MultiModelComparisonRequest;
import com.aihub.admin.dto.response.ModelTestResult;
import com.aihub.admin.dto.response.MultiModelComparisonResponse;
import com.aihub.admin.entity.ModelConfig;
import com.aihub.admin.mapper.ModelConfigMapper;
import com.aihub.admin.service.ModelTestHistoryService;
import com.aihub.ai.infrastructure.ModelGateway;
import com.aihub.common.web.dto.PageResult;
import com.aihub.common.web.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/ai/chat")
public class ModelChatController {

    @Autowired
    private ModelGateway modelGateway;

    @Autowired
    private ModelConfigMapper modelConfigMapper;

    @Autowired
    private ModelTestHistoryService modelTestHistoryService;

    @PostMapping
    public Result<String> chat(@RequestParam Long modelId, @RequestParam String message) {
        long startTime = System.currentTimeMillis();
        try {
            ModelConfig model = modelConfigMapper.selectById(modelId);
            if (model == null) {
                return Result.error("Model not found");
            }
            if (model.getStatus() != 1) {
                return Result.error("Model is disabled");
            }

            String response = modelGateway.chat(
                model.getVendor(),
                model.getModelId(),
                model.getApiKey(),
                model.getBaseUrl(),
                message
            );

            ModelTestResult result = new ModelTestResult();
            result.setModelId(modelId);
            result.setModelName(model.getName());
            result.setVendor(model.getVendor());
            result.setUserMessage(message);
            result.setResponse(response);
            result.setSuccess(true);
            result.setResponseTimeMs(System.currentTimeMillis() - startTime);
            result.setTimestamp(java.time.LocalDateTime.now());

            modelTestHistoryService.saveTestHistory(result);

            return Result.success(response);
        } catch (Exception e) {
            log.error("Chat error: {}", e.getMessage(), e);

            ModelTestResult result = new ModelTestResult();
            result.setModelId(modelId);
            result.setUserMessage(message);
            result.setSuccess(false);
            result.setError(e.getMessage());
            result.setResponseTimeMs(System.currentTimeMillis() - startTime);
            result.setTimestamp(java.time.LocalDateTime.now());

            modelTestHistoryService.saveTestHistory(result);

            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/health")
    public Result<Boolean> healthCheck(@RequestParam Long modelId) {
        ModelConfig model = modelConfigMapper.selectById(modelId);
        if (model == null) {
            return Result.error("Model not found");
        }

        boolean healthy = modelGateway.healthCheck(
            model.getVendor(),
            model.getModelId(),
            model.getApiKey(),
            model.getBaseUrl()
        );
        return Result.success(healthy);
    }

    @PostMapping("/compare")
    public Result<MultiModelComparisonResponse> compareModels(@RequestBody MultiModelComparisonRequest request) {
        try {
            ExecutorService executor = Executors.newFixedThreadPool(request.getModelIds().size());

            List<CompletableFuture<ModelTestResult>> futures = request.getModelIds().stream()
                .map(modelId -> CompletableFuture.supplyAsync(() -> testModel(modelId, request.getMessage()), executor))
                .toList();

            List<ModelTestResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

            executor.shutdown();

            MultiModelComparisonResponse response = new MultiModelComparisonResponse();
            response.setResults(results);
            response.setTotalModels(results.size());
            response.setSuccessCount((int) results.stream().filter(ModelTestResult::getSuccess).count());
            response.setFailureCount(results.size() - response.getSuccessCount());

            modelTestHistoryService.saveBatchTestHistory(results, null);

            return Result.success(response);
        } catch (Exception e) {
            log.error("Model comparison error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    private ModelTestResult testModel(Long modelId, String message) {
        long startTime = System.currentTimeMillis();
        ModelTestResult result = new ModelTestResult();
        result.setTimestamp(LocalDateTime.now());
        result.setUserMessage(message);

        try {
            ModelConfig model = modelConfigMapper.selectById(modelId);
            if (model == null) {
                result.setModelId(modelId);
                result.setSuccess(false);
                result.setError("Model not found");
                result.setResponseTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }

            if (model.getStatus() != 1) {
                result.setModelId(modelId);
                result.setModelName(model.getName());
                result.setVendor(model.getVendor());
                result.setSuccess(false);
                result.setError("Model is disabled");
                result.setResponseTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }

            String response = modelGateway.chat(
                model.getVendor(),
                model.getModelId(),
                model.getApiKey(),
                model.getBaseUrl(),
                message
            );

            result.setModelId(modelId);
            result.setModelName(model.getName());
            result.setVendor(model.getVendor());
            result.setResponse(response);
            result.setSuccess(true);
            result.setResponseTimeMs(System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            result.setModelId(modelId);
            result.setSuccess(false);
            result.setError(e.getMessage());
            result.setResponseTimeMs(System.currentTimeMillis() - startTime);
            log.error("Test model {} failed: {}", modelId, e.getMessage(), e);
            return result;
        }
    }

    @GetMapping("/history")
    public Result<PageResult<ModelTestResult>> getTestHistory(
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        try {
            PageResult<ModelTestResult> result = modelTestHistoryService.getTestHistoryList(modelId, keyword, current, size);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Get test history error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/models")
    public Result<List<String>> getModels(@RequestParam String vendor, @RequestParam String apiKey, @RequestParam(required = false) String baseUrl) {
        try {
            List<String> models = modelGateway.getModels(vendor, apiKey, baseUrl);
            return Result.success(models);
        } catch (Exception e) {
            log.error("Get models error: vendor={}, error={}", vendor, e.getMessage(), e);
            return Result.error("获取模型失败: " + e.getMessage());
        }
    }
}
