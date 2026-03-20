package com.aihub.admin.controller;

import com.aihub.admin.annotation.OperationLog;
import com.aihub.admin.dto.request.CreatePromptTemplateRequest;
import com.aihub.admin.dto.request.PromptTemplateListRequest;
import com.aihub.admin.dto.request.UpdatePromptTemplateRequest;
import com.aihub.admin.dto.response.PromptCategoryResponse;
import com.aihub.admin.dto.response.PromptTemplateResponse;
import com.aihub.admin.service.PromptTemplateService;
import com.aihub.common.web.dto.PageResult;
import com.aihub.common.web.dto.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/prompt")
public class PromptTemplateController {

    @Autowired
    private PromptTemplateService promptTemplateService;

    @GetMapping("/list")
    public Result<PageResult<PromptTemplateResponse>> list(PromptTemplateListRequest request) {
        PageResult<PromptTemplateResponse> result = promptTemplateService.list(request);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<PromptTemplateResponse> getById(@PathVariable Long id) {
        PromptTemplateResponse template = promptTemplateService.getById(id);
        return Result.success(template);
    }

    @OperationLog(module = "Prompt模板管理", operation = "创建模板", recordParams = true)
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody CreatePromptTemplateRequest request) {
        Long id = promptTemplateService.create(request, null);
        return Result.success(id);
    }

    @OperationLog(module = "Prompt模板管理", operation = "更新模板", recordParams = true)
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody UpdatePromptTemplateRequest request) {
        promptTemplateService.update(request);
        return Result.success();
    }

    @OperationLog(module = "Prompt模板管理", operation = "删除模板")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        promptTemplateService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/render")
    public Result<String> renderTemplate(
            @PathVariable Long id,
            @RequestBody Map<String, String> variables) {
        String renderedContent = promptTemplateService.renderTemplate(id, variables);
        return Result.success(renderedContent);
    }

    @GetMapping("/categories")
    public Result<List<PromptCategoryResponse>> listCategories() {
        List<PromptCategoryResponse> categories = promptTemplateService.listCategories();
        return Result.success(categories);
    }
}
