package com.aihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("model_test_history")
public class ModelTestHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long modelId;

    private String modelName;

    private String vendor;

    private String modelDisplayId;

    private String userMessage;

    private String modelResponse;

    private Boolean success;

    private String errorMessage;

    private Long responseTimeMs;

    private Integer tokenUsage;

    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
