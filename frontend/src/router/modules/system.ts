import { $t } from "@/plugins/i18n";
import { system } from "@/router/enums";
const Layout = () => import("@/layout/index.vue");

export default {
  path: "/system",
  component: Layout,
  redirect: "/system/user",
  meta: {
    icon: "ep:setting",
    title: $t("menus.pureSysManagement"),
    rank: system
  },
  children: [
    {
      path: "/system/user",
      name: "SystemUser",
      component: () => import("@/views/system/user/index.vue"),
      meta: {
        title: $t("menus.pureUser")
      }
    }
    // 角色管理 - 未完成，已隐藏
    // {
    //   path: "/system/role",
    //   name: "SystemRole",
    //   component: () => import("@/views/system/role/index.vue"),
    //   meta: {
    //     title: $t("menus.pureRoleManagement")
    //   }
    // },
    // 权限管理 - 未完成，已隐藏
    // {
    //   path: "/system/menu",
    //   name: "SystemMenu",
    //   component: () => import("@/views/system/menu/index.vue"),
    //   meta: {
    //     title: $t("menus.pureMenuManagement")
    //   }
    // }
  ]
} satisfies RouteConfigsTable;
