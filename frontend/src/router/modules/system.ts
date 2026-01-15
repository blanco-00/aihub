// 系统管理相关路由已移至后端动态路由
// 此文件保留为空，避免与动态路由重复
// 如需添加静态系统管理路由，请取消注释并配置

// import { $t } from "@/plugins/i18n";
// import { system } from "@/router/enums";
// const Layout = () => import("@/layout/index.vue");

// export default {
//   path: "/system",
//   component: Layout,
//   redirect: "/system/user",
//   meta: {
//     icon: "ep:setting",
//     title: $t("menus.pureSysManagement"),
//     rank: system,
//     showLink: false // 隐藏静态路由，使用后端动态路由
//   },
//   children: []
// } satisfies RouteConfigsTable;

// 返回空对象，避免路由注册错误
export default {} as RouteConfigsTable;
