package com.aihub.admin.service;

import com.aihub.admin.dto.request.CreatePromptTemplateRequest;
import com.aihub.admin.dto.request.PromptTemplateListRequest;
import com.aihub.admin.dto.request.UpdatePromptTemplateRequest;
import com.aihub.admin.dto.response.PromptCategoryResponse;
import com.aihub.admin.dto.response.PromptTemplateResponse;
import com.aihub.common.web.dto.PageResult;

import java.util.List;
import java.util.Map;

public interface PromptTemplateService {

    /**
     * 分页查询模板列表
     */
    PageResult<PromptTemplateResponse> list(PromptTemplateListRequest request);

    /**
     * 根据ID查询模板详情
     */
    PromptTemplateResponse getById(Long id);

    /**
     * 创建模板
     */
    Long create(CreatePromptTemplateRequest request, Long userId);

    /**
     * 更新模板
     */
    void update(UpdatePromptTemplateRequest request);

    /**
     * 删除模板
     */
    void delete(Long id);

    /**
     * 渲染模板，替换变量
     */
    String renderTemplate(Long id, Map<String, String> variables);

    /**
     * 查询分类列表
     */
    List<PromptCategoryResponse> listCategories();
}
