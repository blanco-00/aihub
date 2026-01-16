package com.aihub.admin.controller;

import com.aihub.common.web.dto.Result;
import com.aihub.admin.dto.request.CreateMenuRequest;
import com.aihub.admin.dto.request.UpdateMenuRequest;
import com.aihub.admin.dto.response.MenuResponse;
import com.aihub.admin.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/menus")
public class MenuController {
    
    @Autowired
    private MenuService menuService;
    
    /**
     * 获取菜单树
     */
    @GetMapping("/tree")
    public Result<List<MenuResponse>> getMenuTree() {
        List<MenuResponse> menus = menuService.getMenuTree();
        return Result.success(menus);
    }
    
    /**
     * 根据ID获取菜单详情
     */
    @GetMapping("/{id}")
    public Result<MenuResponse> getMenuById(@PathVariable Long id) {
        MenuResponse menu = menuService.getMenuById(id);
        return Result.success(menu);
    }
    
    /**
     * 创建菜单
     */
    @PostMapping
    public Result<Void> createMenu(@RequestBody CreateMenuRequest request) {
        menuService.createMenu(request);
        return Result.success();
    }
    
    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    public Result<Void> updateMenu(@PathVariable Long id, @RequestBody UpdateMenuRequest request) {
        menuService.updateMenu(id, request);
        return Result.success();
    }
    
    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }
}
