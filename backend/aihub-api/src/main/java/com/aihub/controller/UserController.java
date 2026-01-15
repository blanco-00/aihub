package com.aihub.controller;

import com.aihub.dto.*;
import com.aihub.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取用户列表（分页、搜索、筛选）
     */
    @GetMapping
    public Result<PageResult<UserListResponse>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long departmentId,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        
        long startTime = System.currentTimeMillis();
        
        // 获取请求到达服务器的时间（从Filter中设置）
        Long requestArrivalTime = (Long) httpRequest.getAttribute("__requestArrivalTime");
        String requestId = (String) httpRequest.getAttribute("__requestId");
        
        // 只记录性能警告（超过1秒的请求）
        if (requestArrivalTime != null) {
            long timeFromArrival = startTime - requestArrivalTime;
            if (timeFromArrival > 1000) {
                log.warn("[性能警告] {} 请求在到达Controller前耗时过长: {}ms", 
                        requestId, timeFromArrival);
            }
        }
        
        UserListRequest request = new UserListRequest();
        request.setCurrent(current);
        request.setSize(size);
        request.setKeyword(keyword);
        request.setPhone(phone);
        request.setRole(role);
        request.setStatus(status);
        request.setDepartmentId(departmentId);
        
        PageResult<UserListResponse> result = userService.getUserList(request);
        
        long totalTime = System.currentTimeMillis() - startTime;
        if (totalTime > 500) {
            log.warn("[性能警告] UserController.getUserList 总耗时: {}ms", totalTime);
        }
        
        return Result.success(result);
    }
    
    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{id}")
    public Result<UserListResponse> getUserById(@PathVariable Long id) {
        UserListResponse user = userService.getUserById(id);
        return Result.success(user);
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    public Result<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        userService.createUser(request);
        return Result.success();
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        userService.updateUser(id, request);
        return Result.success();
    }
    
    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
    
    /**
     * 启用/禁用用户
     */
    @PutMapping("/{id}/status")
    public Result<Void> toggleUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.toggleUserStatus(id, status);
        return Result.success();
    }
}
