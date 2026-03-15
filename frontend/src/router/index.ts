import "@/utils/sso";
import Cookies from "js-cookie";
import { getConfig } from "@/config";
import NProgress from "@/utils/progress";
import { transformI18n } from "@/plugins/i18n";
import { buildHierarchyTree } from "@/utils/tree";
import remainingRouter from "./modules/remaining";
import { useMultiTagsStoreHook } from "@/store/modules/multiTags";
import { usePermissionStoreHook } from "@/store/modules/permission";
import {
  isUrl,
  openLink,
  cloneDeep,
  isAllEmpty,
  storageLocal,
} from "@pureadmin/utils";
import {
  ascending,
  getTopMenu,
  initRouter,
  isOneOfArray,
  getHistoryMode,
  findRouteByPath,
  handleAliveRoute,
  formatTwoStageRoutes,
  formatFlatteningRoutes,
} from "./utils";
import {
  type Router,
  type RouteRecordRaw,
  type RouteComponent,
  createRouter,
} from "vue-router";
import {
  type DataInfo,
  userKey,
  removeToken,
  multipleTabsKey,
} from "@/utils/auth";
import { getInitStatus } from "@/api/init";

/** 自动导入全部静态路由，无需再手动引入！匹配 src/router/modules 目录（任何嵌套级别）中具有 .ts 扩展名的所有文件，除了 remaining.ts 文件
 * 如何匹配所有文件请看：https://github.com/mrmlnc/fast-glob#basic-syntax
 * 如何排除文件请看：https://cn.vitejs.dev/guide/features.html#negative-patterns
 */
const modules: Record<string, any> = import.meta.glob(
  ["./modules/**/*.ts", "!./modules/**/remaining.ts"],
  {
    eager: true,
  },
);

/** 原始静态路由（未做任何处理） */
const routes = [];

Object.keys(modules).forEach((key) => {
  routes.push(modules[key].default);
});

/** 导出处理后的静态路由（三级及以上的路由全部拍成二级） */
export const constantRoutes: Array<RouteRecordRaw> = formatTwoStageRoutes(
  formatFlatteningRoutes(buildHierarchyTree(ascending(routes.flat(Infinity)))),
);

/** 初始的静态路由，用于退出登录时重置路由 */
const initConstantRoutes: Array<RouteRecordRaw> = cloneDeep(constantRoutes);

/** 用于渲染菜单，保持原始层级 */
export const constantMenus: Array<RouteComponent> = ascending(
  routes.flat(Infinity),
).concat(...remainingRouter);

/** 不参与菜单的路由 */
export const remainingPaths = Object.keys(remainingRouter).map((v) => {
  return remainingRouter[v].path;
});

/** 创建路由实例 */
export const router: Router = createRouter({
  history: getHistoryMode(import.meta.env.VITE_ROUTER_HISTORY || "h5"),
  routes: constantRoutes.concat(...(remainingRouter as any)),
  strict: true,
  scrollBehavior(to, from, savedPosition) {
    return new Promise((resolve) => {
      if (savedPosition) {
        return savedPosition;
      } else {
        if (from.meta.saveSrollTop) {
          const top: number =
            document.documentElement.scrollTop || document.body.scrollTop;
          resolve({ left: 0, top });
        }
      }
    });
  },
});

/** 记录已经加载的页面路径 */
const loadedPaths = new Set<string>();

/** 重置已加载页面记录 */
export function resetLoadedPaths() {
  loadedPaths.clear();
}

/** 记录路由初始化状态，防止重复初始化 */
let isInitializing = false;

/** 初始化状态缓存的 key */
const INIT_STATUS_CACHE_KEY = "aihub_init_status_cache";
const INIT_STATUS_CACHE_TIMESTAMP_KEY = "aihub_init_status_cache_timestamp";
const INIT_STATUS_CACHE_SESSION_KEY = "aihub_init_status_cache_session";

/** 缓存有效期：30天（毫秒） */
const CACHE_DURATION = 30 * 24 * 60 * 60 * 1000;

/** 应用启动时的会话 ID（用于检测是否是新会话） */
const SESSION_ID = Date.now().toString();

/** 正在进行的初始化状态检查 Promise */
let initStatusCheckPromise: Promise<boolean> | null = null;

/** 重置路由 */
export function resetRouter() {
  router.clearRoutes();
  for (const route of initConstantRoutes.concat(...(remainingRouter as any))) {
    router.addRoute(route);
  }
  router.options.routes = formatTwoStageRoutes(
    formatFlatteningRoutes(
      buildHierarchyTree(ascending(routes.flat(Infinity))),
    ),
  );
  usePermissionStoreHook().clearAllCachePage();
  resetLoadedPaths();
  isInitializing = false;
}

/** 路由白名单 */
const whiteList = ["/login", "/init"];

const { VITE_HIDE_HOME } = import.meta.env;

router.beforeEach((to: ToRouteType, _from, next) => {
  to.meta.loaded = loadedPaths.has(to.path);

  if (!to.meta.loaded) {
    NProgress.start();
  }

  if (to.meta?.keepAlive) {
    handleAliveRoute(to, "add");
    // 页面整体刷新和点击标签页刷新
    if (_from.name === undefined || _from.name === "Redirect") {
      handleAliveRoute(to);
    }
  }
  const userInfo = storageLocal().getItem<DataInfo<number>>(userKey);
  const externalLink = isUrl(to?.name as string);
  if (!externalLink) {
    to.matched.some((item) => {
      if (!item.meta.title) return "";
      const Title = getConfig().Title;
      if (Title)
        document.title = `${transformI18n(item.meta.title)} | ${Title}`;
      else document.title = transformI18n(item.meta.title);
    });
  }
  /** 如果已经登录并存在登录信息后不能跳转到路由白名单，而是继续保持在当前页面 */
  function toCorrectRoute() {
    whiteList.includes(to.fullPath) ? next(_from.fullPath) : next();
  }
  if (Cookies.get(multipleTabsKey) && userInfo) {
    // 无权限跳转403页面
    if (to.meta?.roles && !isOneOfArray(to.meta?.roles, userInfo?.roles)) {
      next({ path: "/error/403" });
    }
    // 开启隐藏首页后在浏览器地址栏手动输入首页welcome路由则跳转到404页面
    if (VITE_HIDE_HOME === "true" && to.fullPath === "/welcome") {
      next({ path: "/error/404" });
    }
    if (_from?.name) {
      // name为超链接
      if (externalLink) {
        openLink(to?.name as string);
        NProgress.done();
      } else {
        toCorrectRoute();
      }
    } else {
      // 刷新
      if (
        usePermissionStoreHook().wholeMenus.length === 0 &&
        to.path !== "/login" &&
        to.path !== "/init" &&
        !isInitializing
      ) {
        isInitializing = true;
        initRouter()
          .then((router: Router) => {
            if (!useMultiTagsStoreHook().getMultiTagsCache) {
              const { path } = to;
              const route = findRouteByPath(
                path,
                router.options.routes[0].children,
              );
              getTopMenu(true);
              // query、params模式路由传参数的标签页不在此处处理
              if (route && route.meta?.title) {
                if (isAllEmpty(route.parentId) && route.meta?.backstage) {
                  // 此处为动态顶级路由（目录）
                  const { path, name, meta } = route.children[0];
                  useMultiTagsStoreHook().handleTags("push", {
                    path,
                    name,
                    meta,
                  });
                } else {
                  const { path, name, meta } = route;
                  useMultiTagsStoreHook().handleTags("push", {
                    path,
                    name,
                    meta,
                  });
                }
              }
            }
            isInitializing = false;
            // 确保动态路由完全加入路由列表并且不影响静态路由（注意：动态路由刷新时router.beforeEach可能会触发两次，第一次触发动态路由还未完全添加，第二次动态路由才完全添加到路由列表，如果需要在router.beforeEach做一些判断可以在to.name存在的条件下去判断，这样就只会触发一次）
            if (isAllEmpty(to.name)) {
              router.push(to.fullPath);
            }
            toCorrectRoute();
          })
          .catch(() => {
            // 初始化失败时也要继续路由，避免阻塞
            isInitializing = false;
            toCorrectRoute();
          });
      } else {
        toCorrectRoute();
      }
    }
  } else {
    // 未登录时的处理
    if (to.path === "/login") {
      // 登录页面直接放行
      next();
    } else if (to.path === "/init") {
      // 初始化页面直接放行（不需要检查，因为如果已初始化，会在其他路由被拦截）
      next();
    } else if (whiteList.indexOf(to.path) !== -1) {
      // 白名单页面直接放行
      next();
    } else {
      // 其他页面：先检查初始化状态，再决定跳转到哪里
      checkInitStatusAndRedirect(to, next);
    }
  }
});

/**
 * 从 localStorage 读取缓存的初始化状态
 */
function getCachedInitStatus(): boolean | null {
  try {
    const cached = localStorage.getItem(INIT_STATUS_CACHE_KEY);
    const timestamp = localStorage.getItem(INIT_STATUS_CACHE_TIMESTAMP_KEY);
    const lastSession = sessionStorage.getItem(INIT_STATUS_CACHE_SESSION_KEY);

    // 如果是新会话（前端重启），清除缓存，强制重新查询
    if (lastSession !== SESSION_ID) {
      // 保存当前会话 ID
      sessionStorage.setItem(INIT_STATUS_CACHE_SESSION_KEY, SESSION_ID);
      // 清除旧缓存，强制重新查询
      if (cached !== null || timestamp !== null) {
        localStorage.removeItem(INIT_STATUS_CACHE_KEY);
        localStorage.removeItem(INIT_STATUS_CACHE_TIMESTAMP_KEY);
      }
      return null;
    }

    if (cached === null || timestamp === null) {
      return null;
    }

    const cacheTime = parseInt(timestamp, 10);
    const currentTime = Date.now();

    // 检查缓存是否过期
    if (currentTime - cacheTime > CACHE_DURATION) {
      // 缓存过期，清除
      localStorage.removeItem(INIT_STATUS_CACHE_KEY);
      localStorage.removeItem(INIT_STATUS_CACHE_TIMESTAMP_KEY);
      return null;
    }

    // 缓存有效，返回缓存的值
    return cached === "true";
  } catch (error) {
    console.error("读取初始化状态缓存失败", error);
    return null;
  }
}

/**
 * 将初始化状态保存到 localStorage
 */
function setCachedInitStatus(initialized: boolean): void {
  try {
    localStorage.setItem(INIT_STATUS_CACHE_KEY, initialized ? "true" : "false");
    localStorage.setItem(
      INIT_STATUS_CACHE_TIMESTAMP_KEY,
      Date.now().toString(),
    );
    // 保存当前会话 ID
    sessionStorage.setItem(INIT_STATUS_CACHE_SESSION_KEY, SESSION_ID);
  } catch (error) {
    console.error("保存初始化状态缓存失败", error);
  }
}

/**
 * 后台更新初始化状态缓存（不阻塞用户操作）
 */
let backgroundRefreshPromise: Promise<boolean | null> | null = null;

function refreshInitStatusInBackground(): void {
  // 如果正在后台更新，跳过
  if (backgroundRefreshPromise) {
    return;
  }

  // 后台异步更新缓存（使用独立的 Promise，不影响主流程）
  backgroundRefreshPromise = (async () => {
    try {
      const response = await getInitStatus();
      const initialized = response.code === 200 && response.data === true;
      // 更新缓存
      setCachedInitStatus(initialized);
      return initialized;
    } catch (error) {
      console.error("后台更新初始化状态失败", error);
      // 更新失败不影响使用，继续使用旧缓存
      return null;
    } finally {
      // 清除后台更新 Promise，允许下次更新
      backgroundRefreshPromise = null;
    }
  })();
}

/**
 * 检查系统初始化状态并决定跳转
 * 如果已初始化，跳转到登录页；如果未初始化，跳转到初始化页面
 */
async function checkInitStatusAndRedirect(
  to: ToRouteType,
  next: (to?: string | { path: string }) => void,
) {
  // 先检查本地缓存
  const cachedStatus = getCachedInitStatus();
  if (cachedStatus !== null) {
    // 有有效缓存，立即使用（快速响应）
    if (cachedStatus) {
      // 已初始化，跳转到登录页
      next("/login");
    } else {
      // 未初始化，跳转到初始化页面
      next("/init");
    }

    // 在后台异步更新缓存（不阻塞用户操作）
    // 这样既能快速响应，又能保持缓存新鲜度
    refreshInitStatusInBackground();
    return;
  }

  // 没有缓存，需要等待请求完成
  // 如果正在检查，等待检查完成
  if (initStatusCheckPromise) {
    try {
      const initialized = await initStatusCheckPromise;
      if (initialized) {
        next("/login");
      } else {
        next("/init");
      }
    } catch (error) {
      console.error("等待初始化状态检查失败", error);
      // 检查失败时，默认认为未初始化，跳转到初始化页面
      next("/init");
    }
    return;
  }

  // 开始检查初始化状态
  initStatusCheckPromise = (async () => {
    try {
      const response = await getInitStatus();
      const initialized = response.code === 200 && response.data === true;
      // 保存到本地缓存（30天有效期）
      setCachedInitStatus(initialized);
      return initialized;
    } catch (error) {
      console.error("检查初始化状态失败", error);
      // 检查失败时，默认认为未初始化
      return false;
    } finally {
      // 清除检查 Promise，允许下次检查
      initStatusCheckPromise = null;
    }
  })();

  try {
    const initialized = await initStatusCheckPromise;
    if (initialized) {
      // 已初始化，跳转到登录页
      next("/login");
    } else {
      // 未初始化，跳转到初始化页面
      next("/init");
    }
  } catch (error) {
    console.error("获取初始化状态失败", error);
    // 出错时，默认跳转到初始化页面
    next("/init");
  }
}

/**
 * 清除初始化状态缓存（在创建超级管理员后调用）
 */
export function clearInitStatusCache() {
  try {
    localStorage.removeItem(INIT_STATUS_CACHE_KEY);
    localStorage.removeItem(INIT_STATUS_CACHE_TIMESTAMP_KEY);
    sessionStorage.removeItem(INIT_STATUS_CACHE_SESSION_KEY);
  } catch (error) {
    console.error("清除初始化状态缓存失败", error);
  }
  initStatusCheckPromise = null;
}

router.afterEach((to) => {
  loadedPaths.add(to.path);
  NProgress.done();
});

export default router;
