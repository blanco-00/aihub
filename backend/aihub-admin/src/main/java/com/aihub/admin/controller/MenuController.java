package com.aihub.admin.controller;

import com.aihub.common.web.dto.Result;
import com.aihub.admin.dto.request.CreateMenuRequest;
import com.aihub.admin.dto.request.UpdateMenuRequest;
import com.aihub.admin.dto.response.MenuResponse;
import com.aihub.admin.dto.response.RouteMeta;
import com.aihub.admin.dto.response.RouteResponse;
import com.aihub.admin.service.MenuService;
import com.aihub.common.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取菜单树（根据用户角色）
     * 用于前端动态路由和菜单显示
     */
    @GetMapping("/tree")
    public Result<List<RouteResponse>> getMenuTree(HttpServletRequest request) {
        try {
            // 从请求头获取Token
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // 从Token中获取用户角色
            String roleCode = null;
            if (token != null) {
                try {
                    roleCode = jwtUtil.getRoleFromToken(token);
                } catch (Exception e) {
                    // Token解析失败，使用默认角色
                }
            }
            
            // 如果无法获取角色，使用默认角色（SUPER_ADMIN）
            if (roleCode == null) {
                roleCode = "SUPER_ADMIN";
            }
            
            // 从数据库获取菜单树（根据角色过滤）
            List<MenuResponse> menuTree = menuService.getMenuTreeByRoleCode(roleCode);
            
            // 转换为路由格式
            List<RouteResponse> routes = convertToRoutes(menuTree);
            
            return Result.success(routes);
        } catch (Exception e) {
            log.error("获取菜单树失败", e);
            return Result.success(new ArrayList<>());
        }
    }
    
    /**
     * 获取所有菜单树（用于菜单管理页面，不根据角色过滤）
     */
    @GetMapping("/tree/all")
    public Result<List<MenuResponse>> getAllMenuTree() {
        List<MenuResponse> menus = menuService.getMenuTree();
        return Result.success(menus);
    }
    
    /**
     * 将菜单树转换为路由格式
     */
    private List<RouteResponse> convertToRoutes(List<MenuResponse> menuTree) {
        List<RouteResponse> routes = new ArrayList<>();
        
        for (MenuResponse menu : menuTree) {
            RouteResponse route = new RouteResponse();
            route.setPath(menu.getPath());
            route.setName(menu.getName());
            
            // 如果有子菜单，设置为Layout；否则使用组件路径
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                route.setComponent("Layout");
                route.setChildren(convertToRoutes(menu.getChildren()));
            } else {
                route.setComponent(menu.getComponent());
            }
            
            // 设置重定向
            if (menu.getRedirect() != null) {
                route.setRedirect(menu.getRedirect());
            }
            
            // 设置Meta信息
            RouteMeta meta = new RouteMeta();
            meta.setIcon(menu.getIcon());
            meta.setTitle(menu.getTitle());
            meta.setSortOrder(menu.getSortOrder());
            meta.setShowLink(menu.getShowLink() == 1);
            meta.setKeepAlive(menu.getKeepAlive() == 1);
            // 所有菜单对所有角色可见（已在数据库层面过滤）
            meta.setRoles(null);
            route.setMeta(meta);
            
            routes.add(route);
        }
        
        return routes;
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
