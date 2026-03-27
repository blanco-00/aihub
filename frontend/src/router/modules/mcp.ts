import { $t } from "@/plugins/i18n";
import { mcp } from "@/router/enums";

export default {
  path: "/mcp",
  redirect: "/mcp/index",
  meta: {
    icon: "ri/tools-line",
    title: $t("menus.pureMcp"),
    rank: mcp,
    showLink: true,
  },
  children: [
    {
      path: "/mcp/index",
      name: "McpIndex",
      component: () => import("@/views/mcp/index.vue"),
      meta: {
        title: $t("menus.pureMcp"),
      },
    },
  ],
} satisfies RouteConfigsTable;
