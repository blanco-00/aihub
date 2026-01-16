package com.aihub.admin.service.impl;

import com.aihub.admin.config.DepartmentConfig;
import com.aihub.admin.dto.request.CreateDepartmentRequest;
import com.aihub.admin.dto.response.DepartmentResponse;
import com.aihub.admin.dto.request.UpdateDepartmentRequest;
import com.aihub.admin.entity.Department;
import com.aihub.common.web.exception.BusinessException;
import com.aihub.admin.mapper.DepartmentMapper;
import com.aihub.admin.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 */
@Slf4j
@Service
public class DepartmentServiceImpl implements DepartmentService {
    
    @Autowired
    private DepartmentMapper departmentMapper;
    
    @Autowired
    private DepartmentConfig departmentConfig;
    
    @Override
    public List<DepartmentResponse> getDepartmentTree() {
        List<Department> allDepartments = departmentMapper.selectAllDepartments();
        return buildDepartmentTree(allDepartments, 0L);
    }
    
    @Override
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null || department.getIsDeleted() == 1) {
            log.warn("查询部门失败，部门不存在: id={}", id);
            throw new BusinessException("部门不存在");
        }
        return convertToResponse(department);
    }
    
    @Override
    @Transactional
    public void createDepartment(CreateDepartmentRequest request) {
        // 检查部门名称是否已存在
        Department existingDept = departmentMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Department>()
                .eq(Department::getName, request.getName())
                .eq(Department::getIsDeleted, 0)
        );
        if (existingDept != null) {
            log.warn("创建部门失败，部门名称已存在: name={}, existingDeptId={}", 
                request.getName(), existingDept.getId());
            throw new BusinessException("部门名称已存在");
        }
        
        // 检查父部门是否存在并验证层级深度
        Long parentId = request.getParentId() != null && request.getParentId() > 0 
            ? request.getParentId() : 0L;
        
        if (parentId > 0) {
            Department parentDept = departmentMapper.selectById(parentId);
            if (parentDept == null || parentDept.getIsDeleted() == 1) {
                log.warn("创建部门失败，父部门不存在: parentId={}, deptName={}", 
                    parentId, request.getName());
                throw new BusinessException("父部门不存在");
            }
            
            // 验证层级深度：检查父部门的层级深度是否超过限制
            int parentLevel = calculateDepartmentLevel(parentId);
            if (parentLevel >= departmentConfig.getMaxLevel()) {
                log.warn("创建部门失败，层级深度超过限制: deptName={}, parentId={}, parentLevel={}, maxLevel={}", 
                    request.getName(), parentId, parentLevel, departmentConfig.getMaxLevel());
                throw new BusinessException(
                    String.format("部门层级深度不能超过%d层，当前父部门已处于第%d层", 
                        departmentConfig.getMaxLevel(), parentLevel + 1));
            }
        }
        
        Department department = new Department();
        BeanUtils.copyProperties(request, department);
        department.setParentId(parentId);
        department.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        department.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());
        department.setIsDeleted(0);
        
        departmentMapper.insert(department);
        log.info("创建部门成功: id={}, name={}", department.getId(), department.getName());
    }
    
    @Override
    @Transactional
    public void updateDepartment(Long id, UpdateDepartmentRequest request) {
        Department department = departmentMapper.selectById(id);
        if (department == null || department.getIsDeleted() == 1) {
            log.warn("更新部门失败，部门不存在: id={}", id);
            throw new BusinessException("部门不存在");
        }
        
        // 如果修改了部门名称，检查是否重复
        if (request.getName() != null && !request.getName().equals(department.getName())) {
            Department existingDept = departmentMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Department>()
                    .eq(Department::getName, request.getName())
                    .eq(Department::getIsDeleted, 0)
                    .ne(Department::getId, id)
            );
            if (existingDept != null) {
                log.warn("更新部门失败，部门名称已存在: deptId={}, newName={}, existingDeptId={}", 
                    id, request.getName(), existingDept.getId());
                throw new BusinessException("部门名称已存在");
            }
        }
        
        // 检查父部门是否存在（不能设置自己为父部门）并验证层级深度
        if (request.getParentId() != null) {
            Long newParentId = request.getParentId();
            
            if (newParentId.equals(id)) {
                log.warn("更新部门失败，不能设置自己为父部门: deptId={}, parentId={}", 
                    id, newParentId);
                throw new BusinessException("不能设置自己为父部门");
            }
            
            // 检查是否将父部门设置为自己的子部门（防止循环引用）
            if (newParentId > 0 && isDescendant(newParentId, id)) {
                log.warn("更新部门失败，不能将父部门设置为自己的子部门: deptId={}, parentId={}", 
                    id, newParentId);
                throw new BusinessException("不能将父部门设置为自己的子部门，这会导致循环引用");
            }
            
            if (newParentId > 0) {
                Department parentDept = departmentMapper.selectById(newParentId);
                if (parentDept == null || parentDept.getIsDeleted() == 1) {
                    log.warn("更新部门失败，父部门不存在: deptId={}, parentId={}", 
                        id, newParentId);
                    throw new BusinessException("父部门不存在");
                }
                
                // 验证层级深度：检查父部门的层级深度是否超过限制
                int parentLevel = calculateDepartmentLevel(newParentId);
                if (parentLevel >= departmentConfig.getMaxLevel()) {
                    log.warn("更新部门失败，层级深度超过限制: deptId={}, deptName={}, parentId={}, parentLevel={}, maxLevel={}", 
                        id, department.getName(), newParentId, parentLevel, departmentConfig.getMaxLevel());
                    throw new BusinessException(
                        String.format("部门层级深度不能超过%d层，当前父部门已处于第%d层", 
                            departmentConfig.getMaxLevel(), parentLevel + 1));
                }
            }
        }
        
        // 更新部门
        if (request.getName() != null) {
            department.setName(request.getName());
        }
        if (request.getParentId() != null) {
            department.setParentId(request.getParentId());
        }
        if (request.getSortOrder() != null) {
            department.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            department.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            department.setRemark(request.getRemark());
        }
        department.setUpdatedAt(LocalDateTime.now());
        
        departmentMapper.updateById(department);
        log.info("更新部门成功: id={}, name={}", department.getId(), department.getName());
    }
    
    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null || department.getIsDeleted() == 1) {
            log.warn("删除部门失败，部门不存在: id={}", id);
            throw new BusinessException("部门不存在");
        }
        
        // 检查是否存在子部门
        Long childrenCount = departmentMapper.countChildren(id);
        if (childrenCount > 0) {
            log.warn("删除部门失败，存在子部门: deptId={}, childrenCount={}", id, childrenCount);
            throw new BusinessException("存在子部门，无法删除");
        }
        
        // 逻辑删除
        department.setIsDeleted(1);
        department.setUpdatedAt(LocalDateTime.now());
        departmentMapper.updateById(department);
        log.info("删除部门成功: id={}, name={}", department.getId(), department.getName());
    }
    
    /**
     * 构建部门树
     */
    private List<DepartmentResponse> buildDepartmentTree(List<Department> departments, Long parentId) {
        List<DepartmentResponse> result = new ArrayList<>();
        
        // 按parentId分组
        Map<Long, List<Department>> deptMap = departments.stream()
            .collect(Collectors.groupingBy(Department::getParentId));
        
        // 获取当前层级的部门
        List<Department> currentLevelDepts = deptMap.getOrDefault(parentId, new ArrayList<>());
        
        // 递归构建子部门
        for (Department dept : currentLevelDepts) {
            DepartmentResponse response = convertToResponse(dept);
            List<DepartmentResponse> children = buildDepartmentTree(departments, dept.getId());
            if (!children.isEmpty()) {
                response.setChildren(children);
            }
            result.add(response);
        }
        
        return result;
    }
    
    /**
     * 转换为响应DTO
     */
    private DepartmentResponse convertToResponse(Department department) {
        DepartmentResponse response = new DepartmentResponse();
        BeanUtils.copyProperties(department, response);
        return response;
    }
    
    /**
     * 计算部门的层级深度
     * 从当前部门向上递归查找父部门，直到找到顶级部门（parentId=0）
     * 
     * @param departmentId 部门ID
     * @return 层级深度（从1开始，1表示顶级部门）
     */
    private int calculateDepartmentLevel(Long departmentId) {
        if (departmentId == null || departmentId <= 0) {
            return 0;
        }
        
        int level = 0;
        Long currentId = departmentId;
        // 防止无限循环（理论上不应该发生，但作为安全措施）
        int maxIterations = departmentConfig.getMaxLevel() + 10;
        int iterations = 0;
        
        while (currentId != null && currentId > 0 && iterations < maxIterations) {
            Department dept = departmentMapper.selectById(currentId);
            if (dept == null || dept.getIsDeleted() == 1) {
                break;
            }
            
            level++;
            currentId = dept.getParentId();
            iterations++;
        }
        
        return level;
    }
    
    /**
     * 检查 targetId 是否是 ancestorId 的后代（子部门）
     * 用于防止循环引用：不能将父部门设置为自己的子部门
     * 
     * @param targetId 目标部门ID（可能是新的父部门）
     * @param ancestorId 祖先部门ID（当前部门）
     * @return true 如果 targetId 是 ancestorId 的后代
     */
    private boolean isDescendant(Long targetId, Long ancestorId) {
        if (targetId == null || ancestorId == null || targetId.equals(ancestorId)) {
            return false;
        }
        
        Long currentId = targetId;
        // 防止无限循环
        int maxIterations = departmentConfig.getMaxLevel() + 10;
        int iterations = 0;
        
        while (currentId != null && currentId > 0 && iterations < maxIterations) {
            Department dept = departmentMapper.selectById(currentId);
            if (dept == null || dept.getIsDeleted() == 1) {
                break;
            }
            
            if (dept.getParentId() != null && dept.getParentId().equals(ancestorId)) {
                return true;
            }
            
            currentId = dept.getParentId();
            iterations++;
        }
        
        return false;
    }
}
