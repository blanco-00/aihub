package com.aihub.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ModelTestResult {

    private Long modelId;
    private String modelName;
    private String vendor;
    private String userMessage;
    private String response;
    private Boolean success;
    private String error;
    private Long responseTimeMs;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
