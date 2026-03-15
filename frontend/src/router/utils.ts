import {
  type RouterHistory,
  type RouteRecordRaw,
  type RouteComponent,
  createWebHistory,
  createWebHashHistory,
} from "vue-router";
import { router } from "./index";
import { isProxy, toRaw } from "vue";
import { useTimeoutFn } from "@vueuse/core";
import {
  isString,
  cloneDeep,
  isAllEmpty,
  intersection,
  storageLocal,
  isIncludeAllChildren,
} from "@pureadmin/utils";
import { getConfig } from "@/config";
import { buildHierarchyTree } from "@/utils/tree";
import { userKey, type DataInfo } from "@/utils/auth";
import { type menuType, routerArrays } from "@/layout/types";
import { useMultiTagsStoreHook } from "@/store/modules/multiTags";
import { usePermissionStoreHook } from "@/store/modules/permission";
const Layout = () => import("@/layout/index.vue");
const IFrame = () => import("@/layout/frame.vue");
// https://cn.vitejs.dev/guide/features.html#glob-import
const modulesRoutes = import.meta.glob("/src/views/**/*.{vue,tsx}");

// 动态路由
import { getAsyncRoutes } from "@/api/routes";
// 菜单数据（根据用户角色）
import { getMenuTreeByRole } from "@/api/menu";

function handRank(routeInfo: any) {
  const { name, path, parentId, meta } = routeInfo;
  // 支持 sortOrder（后端字段）和 rank（前端路由字段）
  const rank = meta?.rank ?? meta?.sortOrder;
  return isAllEmpty(parentId)
    ? isAllEmpty(rank) || (rank === 0 && name !== "Home" && path !== "/")
      ? true
      : false
    : false;
}

/** 按照路由中meta下的rank等级升序来排序路由 */
function ascending(arr: any[]) {
  // 过滤掉空对象和无效路由
  const validRoutes = arr.filter((v) => v && (v.path || v.name || v.component));

  validRoutes.forEach((v, index) => {
    // 确保 meta 对象存在
    if (!v.meta) {
      v.meta = {};
    }
    // 如果存在 sortOrder（后端字段），映射到 rank（前端路由字段）
    if (v.meta.sortOrder !== undefined && v.meta.rank === undefined) {
      v.meta.rank = v.meta.sortOrder;
    }
    // 当rank不存在时，根据顺序自动创建，首页路由永远在第一位
    if (handRank(v)) v.meta.rank = index + 2;
  });
  return validRoutes.sort(
    (
      a: { meta: { rank?: number; sortOrder?: number } },
      b: { meta: { rank?: number; sortOrder?: number } },
    ) => {
      // 优先使用 rank，如果没有则使用 sortOrder
      const rankA = a?.meta?.rank ?? a?.meta?.sortOrder ?? 0;
      const rankB = b?.meta?.rank ?? b?.meta?.sortOrder ?? 0;
      return rankA - rankB;
    },
  );
}

/** 过滤meta中showLink为false的菜单 */
function filterTree(data: RouteComponent[]) {
  const newTree = cloneDeep(data).filter(
    (v: { meta: { showLink: boolean } }) => {
      // 如果 meta 不存在或 showLink 未定义，默认显示
      if (!v.meta || v.meta.showLink === undefined) {
        return true;
      }
      return v.meta.showLink !== false;
    },
  );
  newTree.forEach(
    (v: { children }) => v.children && (v.children = filterTree(v.children)),
  );
  return newTree;
}

/** 过滤children长度为0的的目录，当目录下没有菜单时，会过滤此目录，目录没有赋予roles权限，当目录下只要有一个菜单有显示权限，那么此目录就会显示 */
function filterChildrenTree(data: RouteComponent[]) {
  const newTree = cloneDeep(data).filter((v: any) => {
    // 检查是否是目录（Layout）还是叶子节点（实际页面）
    // 在 filterChildrenTree 执行时，component 可能是字符串（如 "Layout" 或 "system/dict/index"）
    const componentStr = v.component as any;
    const isLayout =
      (typeof componentStr === "string" && componentStr === "Layout") ||
      componentStr === Layout ||
      (v.component === undefined && v?.children && v.children.length > 0);

    // 如果是 Layout 目录，需要检查是否有子菜单
    // 如果是叶子节点（有 component 且不是 Layout），即使 children 为空也应该保留
    const hasChildren = v?.children?.length !== 0;
    const isLeafNode =
      !isLayout && v.component !== undefined && v.component !== null;

    // 如果是叶子节点，或者有子菜单，则保留
    const shouldKeep = isLeafNode || hasChildren;

    return shouldKeep;
  });
  newTree.forEach(
    (v: { children }) => v.children && (v.children = filterTree(v.children)),
  );
  return newTree;
}

/** 判断两个数组彼此是否存在相同值 */
function isOneOfArray(a: Array<string> | null | undefined, b: Array<string>) {
  // 如果 roles 为 null 或 undefined，表示所有角色都可以访问（包括没有角色的用户）
  if (a === null || a === undefined) {
    return true;
  }
  // 如果 roles 是空数组，表示没有角色可以访问
  if (!Array.isArray(a) || a.length === 0) {
    return false;
  }
  // 如果用户没有角色（b 为空数组），但菜单要求特定角色，则不允许访问
  // 但如果用户有角色，检查是否有交集
  if (!Array.isArray(b) || b.length === 0) {
    // 用户没有角色，但菜单要求特定角色，不允许访问
    return false;
  }
  // 检查是否有交集
  return intersection(a, b).length > 0;
}

/** 从localStorage里取出当前登录用户的角色roles，过滤无权限的菜单 */
function filterNoPermissionTree(data: RouteComponent[]) {
  if (!data || data.length === 0) {
    return [];
  }

  const currentRoles =
    storageLocal().getItem<DataInfo<number>>(userKey)?.roles ?? [];

  const newTree = cloneDeep(data).filter((v: any) => {
    // 确保 meta 存在
    if (!v.meta) {
      v.meta = {};
    }

    const routeRoles = v.meta.roles;
    const hasPermission = isOneOfArray(routeRoles, currentRoles);
    return hasPermission;
  });

  // 递归处理子菜单
  newTree.forEach((v: any) => {
    if (v.children && Array.isArray(v.children) && v.children.length > 0) {
      v.children = filterNoPermissionTree(v.children);
    }
  });

  const finalTree = filterChildrenTree(newTree);

  return finalTree;
}

/** 通过指定 `key` 获取父级路径集合，默认 `key` 为 `path` */
function getParentPaths(value: string, routes: RouteRecordRaw[], key = "path") {
  // 深度遍历查找
  function dfs(routes: RouteRecordRaw[], value: string, parents: string[]) {
    for (let i = 0; i < routes.length; i++) {
      const item = routes[i];
      // 返回父级path
      if (item[key] === value) return parents;
      // children不存在或为空则不递归
      if (!item.children || !item.children.length) continue;
      // 往下查找时将当前path入栈
      parents.push(item.path);

      if (dfs(item.children, value, parents).length) return parents;
      // 深度遍历查找未找到时当前path 出栈
      parents.pop();
    }
    // 未找到时返回空数组
    return [];
  }

  return dfs(routes, value, []);
}

/** 查找对应 `path` 的路由信息 */
function findRouteByPath(path: string, routes: RouteRecordRaw[]) {
  let res = routes.find((item: { path: string }) => item.path == path);
  if (res) {
    return isProxy(res) ? toRaw(res) : res;
  } else {
    for (let i = 0; i < routes.length; i++) {
      if (
        routes[i].children instanceof Array &&
        routes[i].children.length > 0
      ) {
        res = findRouteByPath(path, routes[i].children);
        if (res) {
          return isProxy(res) ? toRaw(res) : res;
        }
      }
    }
    return null;
  }
}

/** 动态路由注册完成后，再添加全屏404（页面不存在）页面，避免刷新动态路由页面时误跳转到404页面 */
function addPathMatch() {
  if (!router.hasRoute("pathMatch")) {
    router.addRoute({
      path: "/:pathMatch(.*)*",
      name: "PageNotFound",
      component: () => import("@/views/error/404.vue"),
      meta: {
        title: "menus.purePageNotFound",
        showLink: false,
      },
    });
  }
}

/** 数据格式转换：将 MenuResponse 格式转换为 RouteResponse 格式 */
function normalizeRouteData(route: any): any {
  if (!route) return route;

  // 如果顶层有 showLink，说明是 MenuResponse 格式，需要转换为 RouteResponse 格式
  if (route.showLink !== undefined && !route.meta) {
    const normalizedRoute: any = {
      path: route.path,
      name: route.name,
      component: route.component,
      redirect: route.redirect,
      meta: {
        icon: route.icon,
        title: route.title,
        sortOrder: route.sortOrder,
        // 将后端的 sortOrder 映射到前端的 rank（路由系统使用 rank 排序）
        rank: route.sortOrder,
        showLink: route.showLink === 1 || route.showLink === true,
        keepAlive: route.keepAlive === 1 || route.keepAlive === true,
        roles: null, // 所有菜单对所有角色可见（已在数据库层面过滤）
      },
    };
    // 递归处理子菜单（处理 children 为 null 或空数组的情况）
    // 注意：children 应该始终是数组，不能是 null，避免前端组件访问时出错
    if (
      route.children &&
      Array.isArray(route.children) &&
      route.children.length > 0
    ) {
      normalizedRoute.children = route.children.map((child: any) =>
        normalizeRouteData(child),
      );
    } else {
      normalizedRoute.children = [];
    }
    return normalizedRoute;
  }

  // 如果已经是 RouteResponse 格式，确保 meta 对象存在
  if (!route.meta) {
    route.meta = {};
  }

  // 确保 roles 字段：如果后端返回的是 null，保持为 null（表示所有角色可见）
  // 如果后端没有返回 roles 字段，也设置为 null
  if (route.meta.roles === undefined) {
    route.meta.roles = null;
  }

  // 确保 children 始终是数组，不能是 null
  if (route.children === null || route.children === undefined) {
    route.children = [];
  } else if (Array.isArray(route.children) && route.children.length > 0) {
    route.children = route.children.map((child: any) =>
      normalizeRouteData(child),
    );
  } else {
    route.children = [];
  }

  return route;
}

/** 处理动态路由（后端返回的路由） */
function handleAsyncRoutes(routeList) {
  if (!routeList || routeList.length === 0) {
    usePermissionStoreHook().handleWholeMenus([]);
  } else {
    // 数据格式转换：如果后端返回的是 MenuResponse 格式（有 showLink 在顶层），需要转换为 RouteResponse 格式
    const normalizedRoutes = routeList.map((route: any) =>
      normalizeRouteData(route),
    );

    try {
      formatFlatteningRoutes(addAsyncRoutes(cloneDeep(normalizedRoutes))).map(
        (v: RouteRecordRaw) => {
          // 防止重复添加路由
          if (
            router.options.routes[0].children.findIndex(
              (value) => value.path === v.path,
            ) !== -1
          ) {
            return;
          } else {
            // 切记将路由push到routes后还需要使用addRoute，这样路由才能正常跳转
            router.options.routes[0].children.push(v);
            // 最终路由进行升序
            ascending(router.options.routes[0].children);
            if (!router.hasRoute(v?.name)) router.addRoute(v);
            const flattenRouters: any = router
              .getRoutes()
              .find((n) => n.path === "/");
            // 保持router.options.routes[0].children与path为"/"的children一致，防止数据不一致导致异常
            if (flattenRouters) {
              flattenRouters.children = router.options.routes[0].children;
              router.addRoute(flattenRouters);
            }
          }
        },
      );
    } catch (error) {
      console.error("添加路由时出错:", error);
    }

    usePermissionStoreHook().handleWholeMenus(normalizedRoutes);
  }
  if (!useMultiTagsStoreHook().getMultiTagsCache) {
    useMultiTagsStoreHook().handleTags("equal", [
      ...routerArrays,
      ...usePermissionStoreHook().flatteningRoutes.filter(
        (v) => v?.meta?.fixedTag,
      ),
    ]);
  }
  addPathMatch();
}

/** 初始化路由（`new Promise` 写法防止在异步请求中造成无限循环）*/
function initRouter() {
  // 清除旧的缓存数据，避免使用包含 /system 和 /monitor 的旧缓存
  const key = "async-routes";
  storageLocal().removeItem(key);

  if (getConfig()?.CachingAsyncRoutes) {
    // 开启动态路由缓存本地localStorage
    // 注意：缓存已在上面清除，这里强制从接口获取最新数据
    const asyncRouteList = null; // 强制不使用缓存，从接口获取最新数据

    if (asyncRouteList && asyncRouteList?.length > 0) {
      return new Promise((resolve) => {
        // 对缓存的数据也进行格式转换处理
        const normalizedCachedRoutes = asyncRouteList.map((route: any) =>
          normalizeRouteData(route),
        );
        handleAsyncRoutes(normalizedCachedRoutes);
        resolve(router);
      });
    } else {
      return new Promise((resolve) => {
        // 并行获取菜单数据和静态路由配置
        Promise.all([
          getMenuTreeByRole().catch(() => ({ code: 0, data: [] })),
          getAsyncRoutes().catch(() => ({ code: 0, data: [] })),
        ])
          .then(([menuResult, staticRouteResult]) => {
            const menuData =
              menuResult.code === 0 || menuResult.code === 200
                ? menuResult.data
                : [];
            const staticRoutes =
              staticRouteResult.code === 0 || staticRouteResult.code === 200
                ? staticRouteResult.data
                : [];

            // 合并菜单数据和静态路由配置
            // 注意：静态路由不应该包含 /system 和 /monitor，这些应该只从菜单接口获取
            const staticRoutesFiltered = staticRoutes.filter(
              (route: any) =>
                route.path !== "/system" && route.path !== "/monitor",
            );
            const allRoutes = [
              ...(menuData || []),
              ...(staticRoutesFiltered || []),
            ];

            if (allRoutes.length > 0) {
              // 数据格式转换：将 MenuResponse 格式转换为 RouteResponse 格式
              const normalizedRoutes = allRoutes.map((route: any) =>
                normalizeRouteData(route),
              );
              handleAsyncRoutes(cloneDeep(normalizedRoutes));
              // 保存转换后的数据到缓存
              storageLocal().setItem(key, normalizedRoutes);
            } else {
              // 即使没有数据，也要调用 handleAsyncRoutes 来设置 wholeMenus，避免菜单一直转圈
              handleAsyncRoutes([]);
            }
            resolve(router);
          })
          .catch((error) => {
            console.error("获取路由数据失败:", error);
            // 接口失败时也 resolve，避免阻塞路由
            resolve(router);
          });
      });
    }
  } else {
    return new Promise((resolve) => {
      // 并行获取菜单数据和静态路由配置
      Promise.all([
        getMenuTreeByRole().catch(() => ({ code: 0, data: [] })),
        getAsyncRoutes().catch(() => ({ code: 0, data: [] })),
      ])
        .then(([menuResult, staticRouteResult]) => {
          const menuData =
            menuResult.code === 0 || menuResult.code === 200
              ? menuResult.data
              : [];
          const staticRoutes =
            staticRouteResult.code === 0 || staticRouteResult.code === 200
              ? staticRouteResult.data
              : [];

          // 合并菜单数据和静态路由配置
          // 注意：静态路由不应该包含 /system 和 /monitor，这些应该只从菜单接口获取
          const staticRoutesFiltered = staticRoutes.filter(
            (route: any) =>
              route.path !== "/system" && route.path !== "/monitor",
          );
          const allRoutes = [
            ...(menuData || []),
            ...(staticRoutesFiltered || []),
          ];

          if (allRoutes.length > 0) {
            // 数据格式转换：将 MenuResponse 格式转换为 RouteResponse 格式
            const normalizedRoutes = allRoutes.map((route: any) =>
              normalizeRouteData(route),
            );
            handleAsyncRoutes(cloneDeep(normalizedRoutes));
          } else {
            // 即使没有数据，也要调用 handleAsyncRoutes 来设置 wholeMenus，避免菜单一直转圈
            handleAsyncRoutes([]);
          }
          resolve(router);
        })
        .catch(() => {
          // 接口失败时也 resolve，避免阻塞路由
          resolve(router);
        });
    });
  }
}

/**
 * 将多级嵌套路由处理成一维数组
 * @param routesList 传入路由
 * @returns 返回处理后的一维路由
 */
function formatFlatteningRoutes(routesList: RouteRecordRaw[]) {
  if (routesList.length === 0) return routesList;
  let hierarchyList = buildHierarchyTree(routesList);
  for (let i = 0; i < hierarchyList.length; i++) {
    if (hierarchyList[i].children) {
      hierarchyList = hierarchyList
        .slice(0, i + 1)
        .concat(hierarchyList[i].children, hierarchyList.slice(i + 1));
    }
  }
  return hierarchyList;
}

/**
 * 一维数组处理成多级嵌套数组（三级及以上的路由全部拍成二级，keep-alive 只支持到二级缓存）
 * https://github.com/pure-admin/vue-pure-admin/issues/67
 * @param routesList 处理后的一维路由菜单数组
 * @returns 返回将一维数组重新处理成规定路由的格式
 */
function formatTwoStageRoutes(routesList: RouteRecordRaw[]) {
  if (routesList.length === 0) return routesList;
  const newRoutesList: RouteRecordRaw[] = [];
  routesList.forEach((v: RouteRecordRaw) => {
    if (v.path === "/") {
      newRoutesList.push({
        component: v.component,
        name: v.name,
        path: v.path,
        redirect: v.redirect,
        meta: v.meta,
        children: [],
      });
    } else {
      newRoutesList[0]?.children.push({ ...v });
    }
  });
  return newRoutesList;
}

/** 处理缓存路由（添加、删除、刷新） */
function handleAliveRoute({ name }: ToRouteType, mode?: string) {
  switch (mode) {
    case "add":
      usePermissionStoreHook().cacheOperate({
        mode: "add",
        name,
      });
      break;
    case "delete":
      usePermissionStoreHook().cacheOperate({
        mode: "delete",
        name,
      });
      break;
    case "refresh":
      usePermissionStoreHook().cacheOperate({
        mode: "refresh",
        name,
      });
      break;
    default:
      usePermissionStoreHook().cacheOperate({
        mode: "delete",
        name,
      });
      useTimeoutFn(() => {
        usePermissionStoreHook().cacheOperate({
          mode: "add",
          name,
        });
      }, 100);
  }
}

/** 过滤后端传来的动态路由 重新生成规范路由 */
function addAsyncRoutes(arrRoutes: Array<RouteRecordRaw>) {
  if (!arrRoutes || !arrRoutes.length) return;
  const modulesRoutesKeys = Object.keys(modulesRoutes);
  arrRoutes.forEach((v: RouteRecordRaw) => {
    // 确保 meta 对象存在
    if (!v.meta) {
      v.meta = { title: v.meta?.title || "" };
    }
    // 确保 title 存在
    if (!v.meta.title) {
      v.meta.title = "";
    }
    // 将backstage属性加入meta，标识此路由为后端返回路由
    v.meta.backstage = true;
    // 数据转换：确保 showLink 是 boolean 类型（后端可能返回数字 0/1）
    if (v.meta.showLink !== undefined && typeof v.meta.showLink !== "boolean") {
      v.meta.showLink = v.meta.showLink === 1 || v.meta.showLink === true;
    }
    // 数据转换：确保 keepAlive 是 boolean 类型
    if (
      v.meta.keepAlive !== undefined &&
      typeof v.meta.keepAlive !== "boolean"
    ) {
      v.meta.keepAlive = v.meta.keepAlive === 1 || v.meta.keepAlive === true;
    }
    // 父级的redirect属性取值：如果子级存在且父级的redirect属性不存在，默认取第一个子级的path；如果子级存在且父级的redirect属性存在，取存在的redirect属性，会覆盖默认值
    if (v?.children && v.children.length && !v.redirect)
      v.redirect = v.children[0].path;
    // 父级的name属性取值：如果子级存在且父级的name属性不存在，默认取第一个子级的name；如果子级存在且父级的name属性存在，取存在的name属性，会覆盖默认值（注意：测试中发现父级的name不能和子级name重复，如果重复会造成重定向无效（跳转404），所以这里给父级的name起名的时候后面会自动加上`Parent`，避免重复）
    if (v?.children && v.children.length && !v.name)
      v.name = (v.children[0].name as string) + "Parent";
    // 检查并修复父级和子级 name 重复的问题（避免路由名称冲突）
    if (v?.children && v.children.length && v.name) {
      const hasDuplicateName = v.children.some(
        (child: any) => child.name === v.name,
      );
      if (hasDuplicateName) {
        // 如果父级和子级 name 重复，给父级 name 加上 "Parent" 后缀
        v.name = (v.name as string) + "Parent";
      }
    }
    // 处理父级路由的 component：优先检查是否为 "Layout" 字符串，或者没有 component 但有 children 的情况
    const componentStr = v.component as any;
    if (typeof componentStr === "string" && componentStr === "Layout") {
      v.component = Layout;
    } else if (!v.component && v?.children && v.children.length) {
      v.component = Layout;
    } else if (v.meta?.frameSrc) {
      v.component = IFrame;
    } else {
      // 对后端传component组件路径和不传做兼容（如果后端传component组件路径，那么path可以随便写，如果不传，component组件路径会跟path保持一致）
      const index = v?.component
        ? modulesRoutesKeys.findIndex((ev) => ev.includes(v.component as any))
        : modulesRoutesKeys.findIndex((ev) => ev.includes(v.path));
      v.component = modulesRoutes[modulesRoutesKeys[index]];
    }
    // 确保 children 始终是数组，不能是 null，避免前端组件访问时出错
    if (v.children === null || v.children === undefined) {
      v.children = [];
    } else if (Array.isArray(v.children) && v.children.length > 0) {
      // 递归处理子菜单
      addAsyncRoutes(v.children);
    } else {
      v.children = [];
    }
  });
  return arrRoutes;
}

/** 获取路由历史模式 https://next.router.vuejs.org/zh/guide/essentials/history-mode.html */
function getHistoryMode(routerHistory): RouterHistory {
  // 如果 routerHistory 未定义或为空，使用默认值 "h5"
  if (!routerHistory || typeof routerHistory !== "string") {
    return createWebHistory("");
  }
  // len为1 代表只有历史模式 为2 代表历史模式中存在base参数 https://next.router.vuejs.org/zh/api/#%E5%8F%82%E6%95%B0-1
  const historyMode = routerHistory.split(",");
  const leftMode = historyMode[0];
  const rightMode = historyMode[1];
  // no param
  if (historyMode.length === 1) {
    if (leftMode === "hash") {
      return createWebHashHistory("");
    } else if (leftMode === "h5") {
      return createWebHistory("");
    }
  } //has param
  else if (historyMode.length === 2) {
    if (leftMode === "hash") {
      return createWebHashHistory(rightMode);
    } else if (leftMode === "h5") {
      return createWebHistory(rightMode);
    }
  }
  // 默认返回 h5 模式
  return createWebHistory("");
}

/** 获取当前页面按钮级别的权限 */
function getAuths(): Array<string> {
  return router.currentRoute.value.meta.auths as Array<string>;
}

/** 是否有按钮级别的权限（根据路由`meta`中的`auths`字段进行判断）*/
function hasAuth(value: string | Array<string>): boolean {
  if (!value) return false;
  /** 从当前路由的`meta`字段里获取按钮级别的所有自定义`code`值 */
  const metaAuths = getAuths();
  if (!metaAuths) return false;
  const isAuths = isString(value)
    ? metaAuths.includes(value)
    : isIncludeAllChildren(value, metaAuths);
  return isAuths ? true : false;
}

function handleTopMenu(route) {
  if (route?.children && route.children.length > 1) {
    if (route.redirect) {
      return route.children.filter((cur) => cur.path === route.redirect)[0];
    } else {
      return route.children[0];
    }
  } else {
    return route;
  }
}

/** 获取所有菜单中的第一个菜单（顶级菜单）*/
function getTopMenu(tag = false): menuType {
  const topMenu = handleTopMenu(
    usePermissionStoreHook().wholeMenus[0]?.children[0],
  );
  tag && useMultiTagsStoreHook().handleTags("push", topMenu);
  return topMenu;
}

export {
  hasAuth,
  getAuths,
  ascending,
  filterTree,
  initRouter,
  getTopMenu,
  addPathMatch,
  isOneOfArray,
  getHistoryMode,
  addAsyncRoutes,
  getParentPaths,
  findRouteByPath,
  handleAliveRoute,
  formatTwoStageRoutes,
  formatFlatteningRoutes,
  filterNoPermissionTree,
};
