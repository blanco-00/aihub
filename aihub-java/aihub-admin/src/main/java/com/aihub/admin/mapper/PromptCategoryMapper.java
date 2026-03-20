package com.aihub.admin.mapper;

import com.aihub.admin.entity.PromptCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface PromptCategoryMapper extends BaseMapper<PromptCategory> {

    /**
     * 查询所有启用的分类列表
     */
    List<PromptCategory> selectEnabledCategories();
}
