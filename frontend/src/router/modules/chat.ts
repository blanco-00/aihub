import { $t } from "@/plugins/i18n";
import { chat } from "@/router/enums";

export default {
  path: "/chat",
  redirect: "/chat/index",
  meta: {
    icon: "ri/chat-3-line",
    title: $t("menus.pureChat"),
    rank: chat,
    showLink: true,
  },
  children: [
    {
      path: "/chat/index",
      name: "ChatIndex",
      component: () => import("@/views/ai/chat/index.vue"),
      meta: {
        title: $t("menus.pureChat"),
      },
    },
  ],
} satisfies RouteConfigsTable;
