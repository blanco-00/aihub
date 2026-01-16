package com.aihub.admin.controller;

import com.aihub.common.web.dto.Result;
import com.aihub.admin.dto.response.MenuResponse;
import com.aihub.admin.dto.response.RouteMeta;
import com.aihub.admin.dto.response.RouteResponse;
import com.aihub.admin.service.MenuService;
import com.aihub.common.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态路由接口
 * 只返回静态路由配置（时效性不高的数据），如 iframe、tabs 等
 * 菜单数据应从 /api/menus/tree 接口获取
 */
@Slf4j
@RestController
@RequestMapping("/api/routes")
public class RouteController {
    
    /**
     * 获取静态路由配置
     * 只返回时效性不高的静态路由，如 iframe、tabs 等
     * 菜单数据应从 /api/menus/tree 接口获取
     */
    @GetMapping("/async")
    public Result<List<RouteResponse>> getAsyncRoutes() {
        try {
            log.debug("获取静态路由配置");
            // 返回静态路由配置（iframe、tabs 等）
            // 这些路由不需要从数据库查询，是固定的配置
            List<RouteResponse> staticRoutes = getStaticRoutes();
            return Result.success(staticRoutes);
        } catch (Exception e) {
            log.error("获取静态路由配置失败", e);
            return Result.success(new ArrayList<>());
        }
    }
    
    /**
     * 获取静态路由配置
     * 这些路由是固定的，不需要从数据库查询
     */
    private List<RouteResponse> getStaticRoutes() {
        List<RouteResponse> routes = new ArrayList<>();
        
        // iframe 路由
        RouteResponse iframeRoute = new RouteResponse();
        iframeRoute.setPath("/iframe");
        RouteMeta iframeMeta = new RouteMeta();
        iframeMeta.setIcon("ri:links-fill");
        iframeMeta.setTitle("menus.pureExternalPage");
        iframeMeta.setSortOrder(12);
        iframeRoute.setMeta(iframeMeta);
        
        // iframe 子路由
        List<RouteResponse> iframeChildren = new ArrayList<>();
        
        // embedded 子路由
        RouteResponse embeddedRoute = new RouteResponse();
        embeddedRoute.setPath("/iframe/embedded");
        RouteMeta embeddedMeta = new RouteMeta();
        embeddedMeta.setTitle("menus.pureEmbeddedDoc");
        embeddedRoute.setMeta(embeddedMeta);
        
        List<RouteResponse> embeddedChildren = new ArrayList<>();
        // 添加各种 iframe 子路由（colorhunt, uigradients, ep, tailwindcss, vue3, vite, pinia, vue-router）
        String[] iframeNames = {"FrameColorHunt", "FrameUiGradients", "FrameEp", "FrameTailwindcss", "FrameVue", "FrameVite", "FramePinia", "FrameRouter"};
        String[] iframePaths = {"/iframe/colorhunt", "/iframe/uigradients", "/iframe/ep", "/iframe/tailwindcss", "/iframe/vue3", "/iframe/vite", "/iframe/pinia", "/iframe/vue-router"};
        String[] iframeTitles = {"menus.pureColorHuntDoc", "menus.pureUiGradients", "menus.pureEpDoc", "menus.pureTailwindcssDoc", "menus.pureVueDoc", "menus.pureViteDoc", "menus.purePiniaDoc", "menus.pureRouterDoc"};
        String[] iframeSrcs = {
            "https://colorhunt.co/",
            "https://uigradients.com/",
            "https://element-plus.org/zh-CN/",
            "https://tailwindcss.com/docs/installation",
            "https://cn.vuejs.org/",
            "https://cn.vitejs.dev/",
            "https://pinia.vuejs.org/zh/index.html",
            "https://router.vuejs.org/zh/"
        };
        
        for (int i = 0; i < iframeNames.length; i++) {
            RouteResponse iframeChild = new RouteResponse();
            iframeChild.setPath(iframePaths[i]);
            iframeChild.setName(iframeNames[i]);
            RouteMeta iframeChildMeta = new RouteMeta();
            iframeChildMeta.setTitle(iframeTitles[i]);
            iframeChildMeta.setFrameSrc(iframeSrcs[i]);
            iframeChildMeta.setKeepAlive(true);
            iframeChildMeta.setRoles(List.of("admin", "common"));
            iframeChild.setMeta(iframeChildMeta);
            embeddedChildren.add(iframeChild);
        }
        
        embeddedRoute.setChildren(embeddedChildren);
        iframeChildren.add(embeddedRoute);
        
        // external 子路由
        RouteResponse externalRoute = new RouteResponse();
        externalRoute.setPath("/iframe/external");
        RouteMeta externalMeta = new RouteMeta();
        externalMeta.setTitle("menus.pureExternalDoc");
        externalRoute.setMeta(externalMeta);
        
        List<RouteResponse> externalChildren = new ArrayList<>();
        RouteResponse externalLink1 = new RouteResponse();
        externalLink1.setPath("/external");
        externalLink1.setName("https://pure-admin.cn/");
        RouteMeta externalLink1Meta = new RouteMeta();
        externalLink1Meta.setTitle("menus.pureExternalLink");
        externalLink1Meta.setRoles(List.of("admin", "common"));
        externalLink1.setMeta(externalLink1Meta);
        externalChildren.add(externalLink1);
        
        RouteResponse externalLink2 = new RouteResponse();
        externalLink2.setPath("/pureUtilsLink");
        externalLink2.setName("https://pure-admin-utils.netlify.app/");
        RouteMeta externalLink2Meta = new RouteMeta();
        externalLink2Meta.setTitle("menus.pureUtilsLink");
        externalLink2Meta.setRoles(List.of("admin", "common"));
        externalLink2.setMeta(externalLink2Meta);
        externalChildren.add(externalLink2);
        
        externalRoute.setChildren(externalChildren);
        iframeChildren.add(externalRoute);
        
        iframeRoute.setChildren(iframeChildren);
        routes.add(iframeRoute);
        
        // tabs 路由
        RouteResponse tabsRoute = new RouteResponse();
        tabsRoute.setPath("/tabs");
        RouteMeta tabsMeta = new RouteMeta();
        tabsMeta.setIcon("ri:bookmark-2-line");
        tabsMeta.setTitle("menus.pureTabs");
        tabsMeta.setSortOrder(16);
        tabsRoute.setMeta(tabsMeta);
        
        List<RouteResponse> tabsChildren = new ArrayList<>();
        
        RouteResponse tabsIndex = new RouteResponse();
        tabsIndex.setPath("/tabs/index");
        tabsIndex.setName("Tabs");
        RouteMeta tabsIndexMeta = new RouteMeta();
        tabsIndexMeta.setTitle("menus.pureTabs");
        tabsIndexMeta.setRoles(List.of("admin", "common"));
        tabsIndex.setMeta(tabsIndexMeta);
        tabsChildren.add(tabsIndex);
        
        RouteResponse tabsQueryDetail = new RouteResponse();
        tabsQueryDetail.setPath("/tabs/query-detail");
        tabsQueryDetail.setName("TabQueryDetail");
        RouteMeta tabsQueryDetailMeta = new RouteMeta();
        tabsQueryDetailMeta.setShowLink(false);
        tabsQueryDetailMeta.setActivePath("/tabs/index");
        tabsQueryDetailMeta.setRoles(List.of("admin", "common"));
        tabsQueryDetail.setMeta(tabsQueryDetailMeta);
        tabsChildren.add(tabsQueryDetail);
        
        RouteResponse tabsParamsDetail = new RouteResponse();
        tabsParamsDetail.setPath("/tabs/params-detail/:id");
        tabsParamsDetail.setComponent("params-detail");
        tabsParamsDetail.setName("TabParamsDetail");
        RouteMeta tabsParamsDetailMeta = new RouteMeta();
        tabsParamsDetailMeta.setShowLink(false);
        tabsParamsDetailMeta.setActivePath("/tabs/index");
        tabsParamsDetailMeta.setRoles(List.of("admin", "common"));
        tabsParamsDetail.setMeta(tabsParamsDetailMeta);
        tabsChildren.add(tabsParamsDetail);
        
        tabsRoute.setChildren(tabsChildren);
        routes.add(tabsRoute);
        
        return routes;
    }
}
