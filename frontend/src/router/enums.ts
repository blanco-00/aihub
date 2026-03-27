// 完整版菜单比较多，将 rank 抽离出来，在此方便维护

const home = 0, // 平台规定只有 home 路由的 rank 才能为 0 ，所以后端在返回 rank 的时候需要从非 0 开始
  system = 1, // 系统管理
  ai = 2, // AI管理 (旧版，已拆分)
  chat = 3, // AI聊天
  rag = 4, // RAG知识库
  model = 5, // 模型配置
  prompt = 6, // 提示词模板
  agent = 7, // Agent智能体
  mcp = 8, // MCP工具
  vueflow = 9,
  ganttastic = 9,
  components = 10,
  able = 11,
  table = 12,
  form = 13,
  list = 14,
  result = 15,
  error = 16,
  frame = 17,
  nested = 18,
  permission = 19,
  monitor = 20,
  tabs = 21,
  about = 22,
  codemirror = 23,
  markdown = 24,
  editor = 25,
  flowchart = 26,
  formdesign = 27,
  board = 28,
  ppt = 29,
  mind = 30,
  guide = 31,
  menuoverflow = 32;

export {
  home,
  system,
  ai,
  chat,
  rag,
  model,
  prompt,
  agent,
  mcp,
  vueflow,
  ganttastic,
  components,
  able,
  table,
  form,
  list,
  result,
  error,
  frame,
  nested,
  permission,
  monitor,
  tabs,
  about,
  codemirror,
  markdown,
  editor,
  flowchart,
  formdesign,
  board,
  ppt,
  mind,
  guide,
  menuoverflow,
};
