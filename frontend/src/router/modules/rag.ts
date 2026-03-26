import { $t } from "@/plugins/i18n";
import { rag } from "@/router/enums";

export default {
  path: "/rag",
  redirect: "/rag/index",
  meta: {
    icon: "ri/book-2-line",
    title: $t("menus.pureRagKnowledge"),
    rank: rag,
    showLink: true,
  },
  children: [
    {
      path: "/rag/index",
      name: "RagIndex",
      component: () => import("@/views/ai/rag/index.vue"),
      meta: {
        title: $t("menus.pureRagKnowledge"),
      },
    },
  ],
} satisfies RouteConfigsTable;
