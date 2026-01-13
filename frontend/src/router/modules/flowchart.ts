import { $t } from "@/plugins/i18n";
import { flowchart } from "@/router/enums";

export default {
  path: "/flow-chart",
  redirect: "/flow-chart/index",
  meta: {
    icon: "ep/set-up",
    title: $t("menus.pureFlowChart"),
    rank: flowchart,
    showLink: false // 演示功能，隐藏
  },
  children: [
    {
      path: "/flow-chart/index",
      name: "FlowChart",
      component: () => import("@/views/flow-chart/index.vue"),
      meta: {
        title: $t("menus.pureFlowChart")
      }
    }
  ]
} satisfies RouteConfigsTable;
