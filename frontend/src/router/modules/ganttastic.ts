import { $t } from "@/plugins/i18n";
import { ganttastic } from "@/router/enums";

export default {
  path: "/ganttastic",
  redirect: "/ganttastic/index",
  meta: {
    icon: "ri/bar-chart-horizontal-line",
    title: $t("menus.pureGanttastic"),
    rank: ganttastic,
    showLink: false // 演示功能，隐藏
  },
  children: [
    {
      path: "/ganttastic/index",
      name: "Ganttastic",
      component: () => import("@/views/ganttastic/index.vue"),
      meta: {
        title: $t("menus.pureGanttastic")
      }
    }
  ]
} satisfies RouteConfigsTable;
