package com.aihub.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveMessageRequest {

    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    @NotBlank(message = "角色不能为空")
    private String role;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    private Integer tokens;
}
