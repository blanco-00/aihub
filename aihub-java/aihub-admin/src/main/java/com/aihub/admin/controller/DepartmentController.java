package com.aihub.admin.controller;

import com.aihub.admin.annotation.OperationLog;
import com.aihub.common.web.dto.Result;
import com.aihub.admin.dto.request.CreateDepartmentRequest;
import com.aihub.admin.dto.request.UpdateDepartmentRequest;
import com.aihub.admin.dto.response.DepartmentResponse;
import com.aihub.admin.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    
    @Autowired
    private DepartmentService departmentService;
    
    /**
     * 获取部门树
     */
    @GetMapping("/tree")
    public Result<List<DepartmentResponse>> getDepartmentTree() {
        List<DepartmentResponse> departments = departmentService.getDepartmentTree();
        return Result.success(departments);
    }
    
    /**
     * 获取所有部门列表（扁平结构，前端自行处理成树结构）
     */
    @GetMapping
    public Result<List<DepartmentResponse>> getAllDepartments() {
        List<DepartmentResponse> departments = departmentService.getDepartmentTree();
        // 将树结构扁平化
        List<DepartmentResponse> flatList = flattenDepartmentTree(departments);
        return Result.success(flatList);
    }
    
    /**
     * 根据ID获取部门详情
     */
    @GetMapping("/{id}")
    public Result<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        DepartmentResponse department = departmentService.getDepartmentById(id);
        return Result.success(department);
    }
    
    /**
     * 创建部门
     */
    @OperationLog(module = "部门管理", operation = "创建部门", recordParams = true)
    @PostMapping
    public Result<Void> createDepartment(@RequestBody CreateDepartmentRequest request) {
        departmentService.createDepartment(request);
        return Result.success();
    }
    
    /**
     * 更新部门
     */
    @OperationLog(module = "部门管理", operation = "修改部门", recordParams = true)
    @PutMapping("/{id}")
    public Result<Void> updateDepartment(@PathVariable Long id, @RequestBody UpdateDepartmentRequest request) {
        departmentService.updateDepartment(id, request);
        return Result.success();
    }
    
    /**
     * 删除部门
     */
    @OperationLog(module = "部门管理", operation = "删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return Result.success();
    }
    
    /**
     * 将部门树扁平化
     */
    private List<DepartmentResponse> flattenDepartmentTree(List<DepartmentResponse> tree) {
        List<DepartmentResponse> result = new ArrayList<>();
        for (DepartmentResponse dept : tree) {
            DepartmentResponse flatDept = new DepartmentResponse();
            BeanUtils.copyProperties(dept, flatDept);
            flatDept.setChildren(null); // 移除子节点
            result.add(flatDept);
            if (dept.getChildren() != null && !dept.getChildren().isEmpty()) {
                result.addAll(flattenDepartmentTree(dept.getChildren()));
            }
        }
        return result;
    }
}
