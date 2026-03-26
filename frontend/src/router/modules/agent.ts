import { $t } from "@/plugins/i18n";
import { agent } from "@/router/enums";

export default {
  path: "/agent",
  redirect: "/agent/index",
  meta: {
    icon: "ri/robot-line",
    title: $t("menus.pureAgent"),
    rank: agent,
    showLink: false, // 待实现，暂时隐藏
  },
  children: [
    {
      path: "/agent/index",
      name: "AgentIndex",
      component: () => import("@/views/welcome/index.vue"),
      meta: {
        title: $t("menus.pureAgent"),
      },
    },
  ],
} satisfies RouteConfigsTable;
