package com.aihub.admin.service;

import com.aihub.admin.dto.request.MultiModelComparisonRequest;
import com.aihub.admin.dto.response.ModelTestResult;
import com.aihub.common.web.dto.PageResult;

import java.util.List;

public interface ModelTestHistoryService {

    PageResult<ModelTestResult> getTestHistoryList(Long modelId, String keyword, Long current, Long size);

    void saveTestHistory(ModelTestResult result);

    void saveBatchTestHistory(List<ModelTestResult> results, Long userId);
}
