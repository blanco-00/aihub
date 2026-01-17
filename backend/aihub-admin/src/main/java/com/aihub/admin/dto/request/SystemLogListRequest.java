package com.aihub.admin.dto.request;

import lombok.Data;

/**
 * 系统日志列表查询请求DTO
 */
@Data
public class SystemLogListRequest {
    
    private Integer current = 1;
    
    private Integer size = 10;
    
    private String level; // 日志级别 DEBUG/INFO/WARN/ERROR
    
    private String module; // 模块名称
    
    private String startTime; // 开始时间
    
    private String endTime; // 结束时间
}
