package com.aihub.admin.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class MultiModelComparisonResponse {

    private List<ModelTestResult> results;

    private Integer totalModels;

    private Integer successCount;

    private Integer failureCount;
}
