package com.aihub.admin.controller;

import com.aihub.admin.annotation.OperationLog;
import com.aihub.admin.dto.request.OnlineUserListRequest;
import com.aihub.admin.dto.response.OnlineUserResponse;
import com.aihub.admin.service.OnlineUserService;
import com.aihub.common.web.dto.PageResult;
import com.aihub.common.web.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 在线用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/online-users")
public class OnlineUserController {
    
    @Autowired
    private OnlineUserService onlineUserService;
    
    /**
     * 获取在线用户列表（分页、搜索）
     */
    @GetMapping
    public Result<PageResult<OnlineUserResponse>> getOnlineUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username) {
        
        OnlineUserListRequest request = new OnlineUserListRequest();
        request.setCurrent(current);
        request.setSize(size);
        request.setUsername(username);
        
        PageResult<OnlineUserResponse> result = onlineUserService.getOnlineUserList(request);
        return Result.success(result);
    }
    
    /**
     * 强制用户下线
     */
    @OperationLog(module = "在线用户", operation = "强制用户下线", recordParams = true)
    @DeleteMapping("/{userId}")
    public Result<Void> forceOffline(@PathVariable Long userId) {
        onlineUserService.forceOffline(userId);
        return Result.success();
    }
}
