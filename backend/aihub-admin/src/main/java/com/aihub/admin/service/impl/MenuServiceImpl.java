package com.aihub.admin.service.impl;

import com.aihub.admin.dto.request.CreateMenuRequest;
import com.aihub.admin.dto.response.MenuResponse;
import com.aihub.admin.dto.request.UpdateMenuRequest;
import com.aihub.admin.entity.Menu;
import com.aihub.common.web.exception.BusinessException;
import com.aihub.admin.mapper.MenuMapper;
import com.aihub.admin.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Slf4j
@Service
public class MenuServiceImpl implements MenuService {
    
    @Autowired
    private MenuMapper menuMapper;
    
    @Override
    public List<MenuResponse> getMenuTree() {
        List<Menu> allMenus = menuMapper.selectAllMenus();
        return buildMenuTree(allMenus, 0L);
    }
    
    @Override
    public MenuResponse getMenuById(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null || menu.getIsDeleted() == 1) {
            log.warn("查询菜单失败，菜单不存在: id={}", id);
            throw new BusinessException("菜单不存在");
        }
        return convertToResponse(menu);
    }
    
    @Override
    @Transactional
    public void createMenu(CreateMenuRequest request) {
        // 检查菜单名称是否已存在
        Menu existingMenu = menuMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Menu>()
                .eq(Menu::getName, request.getName())
                .eq(Menu::getIsDeleted, 0)
        );
        if (existingMenu != null) {
            log.warn("创建菜单失败，菜单名称已存在: name={}, existingMenuId={}", 
                request.getName(), existingMenu.getId());
            throw new BusinessException("菜单名称已存在");
        }
        
        // 检查父菜单是否存在
        if (request.getParentId() != null && request.getParentId() > 0) {
            Menu parentMenu = menuMapper.selectById(request.getParentId());
            if (parentMenu == null || parentMenu.getIsDeleted() == 1) {
                log.warn("创建菜单失败，父菜单不存在: parentId={}, menuName={}", 
                    request.getParentId(), request.getName());
                throw new BusinessException("父菜单不存在");
            }
        }
        
        Menu menu = new Menu();
        BeanUtils.copyProperties(request, menu);
        menu.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        menu.setIsDeleted(0);
        
        menuMapper.insert(menu);
        log.info("创建菜单成功: id={}, name={}", menu.getId(), menu.getName());
    }
    
    @Override
    @Transactional
    public void updateMenu(Long id, UpdateMenuRequest request) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null || menu.getIsDeleted() == 1) {
            log.warn("更新菜单失败，菜单不存在: id={}", id);
            throw new BusinessException("菜单不存在");
        }
        
        // 如果修改了菜单名称，检查是否重复
        if (request.getName() != null && !request.getName().equals(menu.getName())) {
            Menu existingMenu = menuMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Menu>()
                    .eq(Menu::getName, request.getName())
                    .eq(Menu::getIsDeleted, 0)
                    .ne(Menu::getId, id)
            );
            if (existingMenu != null) {
                log.warn("更新菜单失败，菜单名称已存在: menuId={}, newName={}, existingMenuId={}", 
                    id, request.getName(), existingMenu.getId());
                throw new BusinessException("菜单名称已存在");
            }
        }
        
        // 检查父菜单是否存在（不能设置自己为父菜单）
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                log.warn("更新菜单失败，不能设置自己为父菜单: menuId={}, parentId={}", 
                    id, request.getParentId());
                throw new BusinessException("不能设置自己为父菜单");
            }
            if (request.getParentId() > 0) {
                Menu parentMenu = menuMapper.selectById(request.getParentId());
                if (parentMenu == null || parentMenu.getIsDeleted() == 1) {
                    log.warn("更新菜单失败，父菜单不存在: menuId={}, parentId={}", 
                        id, request.getParentId());
                    throw new BusinessException("父菜单不存在");
                }
            }
        }
        
        // 更新菜单
        if (request.getParentId() != null) {
            menu.setParentId(request.getParentId());
        }
        if (request.getName() != null) {
            menu.setName(request.getName());
        }
        if (request.getPath() != null) {
            menu.setPath(request.getPath());
        }
        if (request.getComponent() != null) {
            menu.setComponent(request.getComponent());
        }
        if (request.getRedirect() != null) {
            menu.setRedirect(request.getRedirect());
        }
        if (request.getIcon() != null) {
            menu.setIcon(request.getIcon());
        }
        if (request.getTitle() != null) {
            menu.setTitle(request.getTitle());
        }
        if (request.getSortOrder() != null) {
            menu.setSortOrder(request.getSortOrder());
        }
        if (request.getShowLink() != null) {
            menu.setShowLink(request.getShowLink());
        }
        if (request.getKeepAlive() != null) {
            menu.setKeepAlive(request.getKeepAlive());
        }
        if (request.getStatus() != null) {
            menu.setStatus(request.getStatus());
        }
        menu.setUpdatedAt(LocalDateTime.now());
        
        menuMapper.updateById(menu);
        log.info("更新菜单成功: id={}, name={}", menu.getId(), menu.getName());
    }
    
    @Override
    @Transactional
    public void deleteMenu(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null || menu.getIsDeleted() == 1) {
            log.warn("删除菜单失败，菜单不存在: id={}", id);
            throw new BusinessException("菜单不存在");
        }
        
        // 检查是否存在子菜单
        Long childrenCount = menuMapper.countChildren(id);
        if (childrenCount > 0) {
            log.warn("删除菜单失败，存在子菜单: menuId={}, childrenCount={}", id, childrenCount);
            throw new BusinessException("存在子菜单，无法删除");
        }
        
        // 逻辑删除
        menu.setIsDeleted(1);
        menu.setUpdatedAt(LocalDateTime.now());
        menuMapper.updateById(menu);
        log.info("删除菜单成功: id={}, name={}", menu.getId(), menu.getName());
    }
    
    @Override
    public List<MenuResponse> getMenuTreeByRoleCode(String roleCode) {
        List<Menu> menus = menuMapper.selectByRoleCode(roleCode);
        
        // 应用层去重：使用 LinkedHashSet 保持顺序并去重（基于菜单ID）
        // 性能优化：在应用层去重，避免 SQL DISTINCT 的开销
        Set<Long> seenIds = new LinkedHashSet<>();
        List<Menu> distinctMenus = menus.stream()
            .filter(menu -> seenIds.add(menu.getId()))
            .collect(Collectors.toList());
        
        List<MenuResponse> result = buildMenuTree(distinctMenus, 0L);
        return result;
    }
    
    /**
     * 构建菜单树
     * 注意：如果子菜单的父菜单不在查询结果中，需要自动包含父菜单
     */
    private List<MenuResponse> buildMenuTree(List<Menu> menus, Long parentId) {
        List<MenuResponse> result = new ArrayList<>();
        
        // 按parentId分组
        Map<Long, List<Menu>> menuMap = menus.stream()
            .collect(Collectors.groupingBy(Menu::getParentId));
        
        // 获取所有菜单的ID集合，用于检查父菜单是否存在
        Set<Long> menuIds = menus.stream()
            .map(Menu::getId)
            .collect(Collectors.toSet());
        
        // 处理缺失父菜单的情况：如果子菜单的父菜单不在结果中，需要查询并包含父菜单
        // 这种情况可能发生在：只关联了子菜单，但没有关联父菜单
        if (parentId == 0) {
            // 顶级菜单：检查是否有子菜单的父菜单不在结果中
            Set<Long> missingParentIds = menus.stream()
                .filter(menu -> menu.getParentId() != null && menu.getParentId() != 0)
                .map(Menu::getParentId)
                .filter(pid -> !menuIds.contains(pid))
                .collect(Collectors.toSet());
            
            // 查询缺失的父菜单
            if (!missingParentIds.isEmpty()) {
                for (Long missingParentId : missingParentIds) {
                    Menu parentMenu = menuMapper.selectById(missingParentId);
                    if (parentMenu != null && parentMenu.getIsDeleted() == 0 && parentMenu.getStatus() == 1) {
                        menus.add(parentMenu);
                        menuIds.add(parentMenu.getId());
                    }
                }
                // 重新分组
                menuMap = menus.stream()
                    .collect(Collectors.groupingBy(Menu::getParentId));
            }
        }
        
        // 获取当前层级的菜单，并按 sortOrder 排序
        List<Menu> currentLevelMenus = menuMap.getOrDefault(parentId, new ArrayList<>());
        // 按 sortOrder 升序排序，如果 sortOrder 相同则按 id 升序排序
        currentLevelMenus.sort((a, b) -> {
            int sortCompare = Integer.compare(
                a.getSortOrder() != null ? a.getSortOrder() : 0,
                b.getSortOrder() != null ? b.getSortOrder() : 0
            );
            if (sortCompare != 0) {
                return sortCompare;
            }
            return Long.compare(
                a.getId() != null ? a.getId() : 0L,
                b.getId() != null ? b.getId() : 0L
            );
        });
        
        // 递归构建子菜单
        for (Menu menu : currentLevelMenus) {
            MenuResponse response = convertToResponse(menu);
            List<MenuResponse> children = buildMenuTree(menus, menu.getId());
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
    private MenuResponse convertToResponse(Menu menu) {
        MenuResponse response = new MenuResponse();
        BeanUtils.copyProperties(menu, response);
        return response;
    }
}
