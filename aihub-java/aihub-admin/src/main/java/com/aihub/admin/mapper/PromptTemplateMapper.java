package com.aihub.admin.mapper;

import com.aihub.admin.entity.PromptTemplate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PromptTemplateMapper extends BaseMapper<PromptTemplate> {

    /**
     * 根据条件查询模板列表
     */
    List<PromptTemplate> selectByCondition(
        @Param("categoryId") Long categoryId,
        @Param("keyword") String keyword,
        @Param("status") Integer status,
        @Param("offset") Long offset,
        @Param("size") Long size
    );

    /**
     * 根据条件统计模板数量
     */
    Long countByCondition(
        @Param("categoryId") Long categoryId,
        @Param("keyword") String keyword,
        @Param("status") Integer status
    );

    /**
     * 根据ID查询模板详情（包含分类名称）
     */
    PromptTemplate selectByIdWithCategory(@Param("id") Long id);
}
