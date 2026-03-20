package com.aihub.admin.service.impl;

import com.aihub.admin.dto.request.CreatePromptTemplateRequest;
import com.aihub.admin.dto.request.PromptTemplateListRequest;
import com.aihub.admin.dto.request.UpdatePromptTemplateRequest;
import com.aihub.admin.dto.response.PromptCategoryResponse;
import com.aihub.admin.dto.response.PromptTemplateResponse;
import com.aihub.admin.entity.PromptCategory;
import com.aihub.admin.entity.PromptTemplate;
import com.aihub.admin.mapper.PromptCategoryMapper;
import com.aihub.admin.mapper.PromptTemplateMapper;
import com.aihub.admin.service.PromptTemplateService;
import com.aihub.common.web.dto.PageResult;
import com.aihub.common.web.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PromptTemplateServiceImpl implements PromptTemplateService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    @Autowired
    private PromptTemplateMapper promptTemplateMapper;

    @Autowired
    private PromptCategoryMapper promptCategoryMapper;

    @Override
    public PageResult<PromptTemplateResponse> list(PromptTemplateListRequest request) {
        long startTime = System.currentTimeMillis();

        Long offset = (request.getCurrent() - 1) * request.getSize();

        List<PromptTemplate> templates = promptTemplateMapper.selectByCondition(
            request.getCategoryId(),
            request.getKeyword(),
            request.getStatus(),
            offset,
            request.getSize()
        );

        Long total = promptTemplateMapper.countByCondition(
            request.getCategoryId(),
            request.getKeyword(),
            request.getStatus()
        );

        Long pages = (total + request.getSize() - 1) / request.getSize();

        List<PromptTemplateResponse> responses = templates.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());

        PageResult<PromptTemplateResponse> result = new PageResult<>();
        result.setRecords(responses);
        result.setTotal(total);
        result.setCurrent(request.getCurrent().intValue());
        result.setSize(request.getSize().intValue());
        result.setPages(pages);

        long totalTime = System.currentTimeMillis() - startTime;
        if (totalTime > 500) {
            log.warn("[性能警告] Prompt模板列表查询总耗时: {}ms", totalTime);
        }

        return result;
    }

    @Override
    public PromptTemplateResponse getById(Long id) {
        PromptTemplate template = promptTemplateMapper.selectByIdWithCategory(id);
        if (template == null || template.getIsDeleted() == 1) {
            throw new BusinessException("模板不存在");
        }
        return convertToResponse(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CreatePromptTemplateRequest request, Long userId) {
        PromptTemplate template = new PromptTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCategoryId(request.getCategoryId());
        template.setContent(request.getContent());
        template.setVariables(request.getVariables());
        template.setIsBuiltin(0);
        template.setStatus(1);
        if (userId == null) {
            template.setCreatedBy(0L);
        } else {
            template.setCreatedBy(userId);
        }
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setIsDeleted(0);

        promptTemplateMapper.insert(template);
        log.info("创建Prompt模板: name={}, createdBy={}", request.getName(), userId != null ? userId : "default");

        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdatePromptTemplateRequest request) {
        PromptTemplate template = promptTemplateMapper.selectById(request.getId());
        if (template == null || template.getIsDeleted() == 1) {
            throw new BusinessException("模板不存在");
        }

        if (template.getIsBuiltin() == 1) {
            throw new BusinessException("内置模板不能修改");
        }

        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCategoryId(request.getCategoryId());
        template.setContent(request.getContent());
        template.setVariables(request.getVariables());
        template.setStatus(request.getStatus());
        template.setUpdatedAt(LocalDateTime.now());

        promptTemplateMapper.updateById(template);
        log.info("更新Prompt模板: id={}", request.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PromptTemplate template = promptTemplateMapper.selectById(id);
        if (template == null || template.getIsDeleted() == 1) {
            throw new BusinessException("模板不存在");
        }

        if (template.getIsBuiltin() == 1) {
            throw new BusinessException("内置模板不能删除");
        }

        template.setIsDeleted(1);
        template.setUpdatedAt(LocalDateTime.now());
        promptTemplateMapper.updateById(template);
        log.info("删除Prompt模板: id={}", id);
    }

    @Override
    public String renderTemplate(Long id, Map<String, String> variables) {
        PromptTemplate template = promptTemplateMapper.selectById(id);
        if (template == null || template.getIsDeleted() == 1) {
            throw new BusinessException("模板不存在");
        }

        if (template.getStatus() == 0) {
            throw new BusinessException("模板已禁用");
        }

        String content = template.getContent();
        if (content == null || content.isEmpty()) {
            return "";
        }

        // 替换 {{变量名}} 占位符
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String replacement = variables != null ? variables.getOrDefault(variableName, "") : "";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        String renderedContent = result.toString();
        log.info("渲染Prompt模板: id={}, variables={}", id, variables);

        return renderedContent;
    }

    @Override
    public List<PromptCategoryResponse> listCategories() {
        List<PromptCategory> categories = promptCategoryMapper.selectEnabledCategories();
        return categories.stream()
            .map(this::convertToCategoryResponse)
            .collect(Collectors.toList());
    }

    private PromptTemplateResponse convertToResponse(PromptTemplate template) {
        PromptTemplateResponse response = new PromptTemplateResponse();
        BeanUtils.copyProperties(template, response);
        return response;
    }

    private PromptCategoryResponse convertToCategoryResponse(PromptCategory category) {
        PromptCategoryResponse response = new PromptCategoryResponse();
        BeanUtils.copyProperties(category, response);
        return response;
    }
}
