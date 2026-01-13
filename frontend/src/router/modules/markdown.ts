import { $t } from "@/plugins/i18n";
import { markdown } from "@/router/enums";

export default {
  path: "/markdown",
  redirect: "/markdown/index",
  meta: {
    icon: "ri/markdown-line",
    title: $t("menus.pureMarkdown"),
    rank: markdown,
    showLink: false // 演示功能，隐藏
  },
  children: [
    {
      path: "/markdown/index",
      name: "Markdown",
      component: () => import("@/views/markdown/index.vue"),
      meta: {
        title: $t("menus.pureMarkdown"),
        extraIcon: "IF-pure-iconfont-new svg"
      }
    }
  ]
} satisfies RouteConfigsTable;
