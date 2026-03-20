package com.aihub.admin.dto.request;

import lombok.Data;

/**
 * 会话列表查询请求DTO
 */
@Data
public class SessionListRequest {

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 搜索关键词（标题）
     */
    private String keyword;

    /**
     * 模型ID筛选
     */
    private Long modelId;
}
