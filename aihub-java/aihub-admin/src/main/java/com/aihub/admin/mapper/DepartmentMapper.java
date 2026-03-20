package com.aihub.admin.mapper;

import com.aihub.admin.entity.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门Mapper接口
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    
    /**
     * 查询所有部门（未删除的）
     */
    List<Department> selectAllDepartments();
    
    /**
     * 根据父部门ID查询子部门列表
     */
    List<Department> selectByParentId(@Param("parentId") Long parentId);
    
    /**
     * 检查是否存在子部门
     */
    Long countChildren(@Param("parentId") Long parentId);
}
