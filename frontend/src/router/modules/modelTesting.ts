import { $t } from "@/plugins/i18n";
import { model } from "@/router/enums";

export default {
  path: "/model-testing",
  redirect: "/model-testing/index",
  meta: {
    icon: "ri/cpu-line",
    title: $t("menus.pureModelTesting"),
    rank: model,
    showLink: true,
  },
  children: [
    {
      path: "/model-testing/index",
      name: "ModelTestingIndex",
      component: () => import("@/views/ai/model-testing/index.vue"),
      meta: {
        title: $t("menus.pureModelTesting"),
      },
    },
  ],
} satisfies RouteConfigsTable;
