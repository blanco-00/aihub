package com.aihub.controller;

import com.aihub.dto.MenuResponse;
import com.aihub.dto.Result;
import com.aihub.dto.RouteMeta;
import com.aihub.dto.RouteResponse;
import com.aihub.service.MenuService;
import com.aihub.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态路由（菜单）接口
 */
@Slf4j
@RestController
@RequestMapping("/api/routes")
public class RouteController {
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取动态路由菜单
     * 从数据库读取菜单，根据用户角色过滤
     */
    @GetMapping("/async")
    public Result<List<RouteResponse>> getAsyncRoutes(HttpServletRequest request) {
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
                    log.debug("无法从Token获取角色，使用默认角色: {}", e.getMessage());
                }
            }
            
            // 如果无法获取角色，使用默认角色（SUPER_ADMIN）
            if (roleCode == null) {
                roleCode = "SUPER_ADMIN";
            }
            
            // 从数据库获取菜单树
            List<MenuResponse> menuTree = menuService.getMenuTreeByRoleCode(roleCode);
            
            // 转换为路由格式
            List<RouteResponse> routes = convertToRoutes(menuTree);
            
            return Result.success(routes);
        } catch (Exception e) {
            log.error("获取动态路由失败", e);
            // 如果出错，返回空列表，前端会使用静态路由
            return Result.success(new ArrayList<>());
        }
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
}
