import { http } from "@/utils/http";

export type ChatSession = {
  id: number;
  title: string;
  userId: number;
  modelId: number;
  modelName: string;
  messageCount: number;
  lastMessageAt: string;
  createdAt: string;
};

export type ChatMessage = {
  id: number;
  sessionId: number;
  role: "user" | "assistant" | "system";
  content: string;
  tokens?: number;
  modelId?: number;
  createdAt: string;
};

export type CreateSessionRequest = {
  title?: string;
  modelId: number;
};

export type SessionListRequest = {
  keyword?: string;
  current: number;
  size: number;
};

export type PageResult<T> = {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
};

export type Result<T> = {
  code: number;
  message: string;
  data: T;
};

/**
 * 获取会话列表
 */
export const getSessionList = (params: SessionListRequest) =>
  http.request<Result<PageResult<ChatSession>>>(
    "get",
    "/api/chat/session/list",
    {
      params,
    },
  );

/**
 * 获取单个会话详情
 */
export const getSession = (id: number) =>
  http.request<Result<ChatSession>>("get", `/api/chat/session/${id}`);

/**
 * 获取会话的消息列表
 */
export const getSessionMessages = (id: number) =>
  http.request<Result<ChatMessage[]>>(
    "get",
    `/api/chat/session/${id}/messages`,
  );

/**
 * 创建新会话
 */
export const createSession = (data: CreateSessionRequest) =>
  http.request<Result<number>>("post", "/api/chat/session/create", { data });

/**
 * 更新会话标题
 */
export const updateSessionTitle = (id: number, title: string) =>
  http.request<Result<void>>("put", `/api/chat/session/${id}/title`, {
    params: { title },
  });

/**
 * 删除会话
 */
export const deleteSession = (id: number) =>
  http.request<Result<void>>("delete", `/api/chat/session/${id}`);

/**
 * 导出会话
 */
export const exportSession = (id: number) => {
  window.open(`/api/chat/session/${id}/export`, "_blank");
};

export type SaveMessageRequest = {
  sessionId: number;
  role: string;
  content: string;
  tokens?: number;
};

export const saveMessage = (data: SaveMessageRequest) =>
  http.request<Result<void>>("post", "/api/chat/session/message/save", { data });
