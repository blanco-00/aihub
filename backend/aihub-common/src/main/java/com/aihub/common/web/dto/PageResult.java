package com.aihub.common.web.dto;

import lombok.Data;
import java.util.List;

/**
 * 分页结果DTO
 */
@Data
public class PageResult<T> {
    
    /**
     * 数据列表
     */
    private List<T> records;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer current;
    
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 总页数
     */
    private Long pages;
}
