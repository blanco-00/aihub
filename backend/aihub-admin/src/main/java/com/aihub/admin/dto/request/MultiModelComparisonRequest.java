package com.aihub.admin.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MultiModelComparisonRequest {

    @NotNull(message = "模型ID列表不能为空")
    @NotEmpty(message = "至少选择一个模型")
    private List<Long> modelIds;

    @NotNull(message = "测试消息不能为空")
    private String message;
}
