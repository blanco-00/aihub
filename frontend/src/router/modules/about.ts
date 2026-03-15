import { $t } from "@/plugins/i18n";
import { about } from "@/router/enums";

export default {
  path: "/about",
  redirect: "/about/index",
  meta: {
    icon: "ri/file-info-line",
    title: $t("menus.pureAbout"),
    rank: about,
    showLink: false, // 演示功能，隐藏
  },
  children: [
    {
      path: "/about/index",
      name: "About",
      component: () => import("@/views/about/index.vue"),
      meta: {
        title: $t("menus.pureAbout"),
      },
    },
  ],
} satisfies RouteConfigsTable;
