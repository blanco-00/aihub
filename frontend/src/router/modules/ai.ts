import { ai } from "@/router/enums";

export default {
  path: "/ai",
  redirect: "/ai/prompt-templates",
  meta: {
    icon: "ri/robot-line",
    title: "menus.hspromptTemplates",
    rank: ai,
    showLink: true,
  },
  children: [
    {
      path: "/ai/prompt-templates",
      name: "PromptTemplates",
      component: () => import("@/views/ai/prompt-templates/index.vue"),
      meta: {
        title: "menus.hspromptTemplates",
        icon: "ri/file-text-line",
      },
    },
    {
      path: "/ai/model-testing",
      name: "ModelTesting",
      component: () => import("@/views/ai/model-testing/index.vue"),
      meta: {
        title: "menus.hsmodelTesting",
        icon: "ri/cpu-line",
      },
    },
  ],
} satisfies RouteConfigsTable;
