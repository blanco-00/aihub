import { $t } from "@/plugins/i18n";
import { prompt } from "@/router/enums";

export default {
  path: "/prompt",
  redirect: "/prompt/index",
  meta: {
    icon: "ri/file-text-line",
    title: $t("menus.purePromptTemplates"),
    rank: prompt,
    showLink: true,
  },
  children: [
    {
      path: "/prompt/index",
      name: "PromptIndex",
      component: () => import("@/views/ai/prompt-templates/index.vue"),
      meta: {
        title: $t("menus.purePromptTemplates"),
      },
    },
  ],
} satisfies RouteConfigsTable;
