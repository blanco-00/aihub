package com.aihub.admin.mapper;

import com.aihub.admin.entity.ModelConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ModelConfigMapper extends BaseMapper<ModelConfig> {

    /**
     * 根据厂商和模型ID查询模型配置
     */
    ModelConfig findByVendorAndModelId(@Param("vendor") String vendor, @Param("modelId") String modelId);

    /**
     * 查询启用的模型配置列表
     */
    List<ModelConfig> selectEnabledModels();

    /**
     * 统计启用的模型配置数量
     */
    Long countEnabledModels();
}
