package com.aihub.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 部门配置类
 * 
 * 配置说明：
 * - max-level: 部门最大层级深度，默认5层
 * - 大多数企业的组织架构在2-5层之间，5层可以满足99%的需求
 * - 如需调整，可在 application.yml 中修改 department.max-level 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "department")
public class DepartmentConfig {
    
    /**
     * 部门最大层级深度
     * 默认值：5层
     * 说明：
     * - 第1层：总部/公司
     * - 第2层：事业部/区域
     * - 第3层：部门
     * - 第4层：科室/小组
     * - 第5层：子小组（可选）
     */
    private Integer maxLevel = 5;
}
