/**
 * Agent Template API - Python AI 服务模板管理
 * 提供Agent模板的CRUD和基于模板的聊天功能
 */
import { getToken } from "@/utils/auth";

// ============================================================
// 类型定义
// ============================================================

/** Agent模板配置 */
export type AgentTemplate = {
  id: number;
  name: string;
  description: string;
  /** 模型名称 */
  model: string;
  /** 系统提示词 */
  systemPrompt: string;
  /** 温度参数 */
  temperature?: number;
  /** 最大token数 */
  maxTokens?: number;
  /** 启用技能列表 */
  enabledSkills?: number[];
  /** 启用MCP工具列表 */
  enabledMcpTools?: string[];
  /** 状态: 0-禁用, 1-启用 */
  status: number;
  createdAt: string;
  updatedAt?: string;
};

/** 创建模板请求 */
export type CreateTemplateRequest = {
  name: string;
  description: string;
  model: string;
  systemPrompt: string;
  temperature?: number;
  maxTokens?: number;
  enabledSkills?: number[];
  enabledMcpTools?: string[];
};

/** 更新模板请求 */
export type UpdateTemplateRequest = {
  id: number;
  name: string;
  description: string;
  model: string;
  systemPrompt: string;
  temperature?: number;
  maxTokens?: number;
  enabledSkills?: number[];
  enabledMcpTools?: string[];
  status: number;
};

/** 基于模板聊天请求 */
export type ChatWithTemplateRequest = {
  templateId: number;
  message: string;
  sessionId?: string;
  /** 额外的系统变量 */
  variables?: Record<string, string>;
};

// ============================================================
// API 函数
// ============================================================

/** 获取 Authorization 请求头 */
const getAuthHeaders = () => {
  const token = getToken();
  const tokenStr = token?.accessToken ? `Bearer ${token.accessToken}` : "";
  return {
    "Content-Type": "application/json",
    Authorization: tokenStr,
  };
};

/**
 * 获取模板列表
 */
export const getTemplates = async (): Promise<AgentTemplate[]> => {
  const res = await fetch("http://localhost:8001/api/agent/templates", {
    method: "GET",
    headers: getAuthHeaders(),
  });
  const data = await res.json();
  return data.data || [];
};

/**
 * 获取单个模板
 */
export const getTemplate = async (id: number): Promise<AgentTemplate | null> => {
  const res = await fetch(`http://localhost:8001/api/agent/templates/${id}`, {
    method: "GET",
    headers: getAuthHeaders(),
  });
  const data = await res.json();
  return data.data || null;
};

/**
 * 创建模板
 */
export const createTemplate = async (
  template: CreateTemplateRequest,
): Promise<number> => {
  const res = await fetch("http://localhost:8001/api/agent/templates", {
    method: "POST",
    headers: getAuthHeaders(),
    body: JSON.stringify(template),
  });
  const data = await res.json();
  return data.data;
};

/**
 * 更新模板
 */
export const updateTemplate = async (template: UpdateTemplateRequest): Promise<void> => {
  await fetch(`http://localhost:8001/api/agent/templates/${template.id}`, {
    method: "PUT",
    headers: getAuthHeaders(),
    body: JSON.stringify(template),
  });
};

/**
 * 删除模板
 */
export const deleteTemplate = async (id: number): Promise<void> => {
  await fetch(`http://localhost:8001/api/agent/templates/${id}`, {
    method: "DELETE",
    headers: getAuthHeaders(),
  });
};

/**
 * 基于模板聊天 - 标准响应
 */
export const chatWithTemplate = async (
  request: ChatWithTemplateRequest,
): Promise<{ answer: string }> => {
  const res = await fetch("http://localhost:8001/api/agent/chat/template", {
    method: "POST",
    headers: getAuthHeaders(),
    body: JSON.stringify(request),
  });
  const data = await res.json();
  return data.data || { answer: "" };
};

/**
 * 基于模板聊天 - 流式响应
 */
export const chatWithTemplateStream = (
  request: ChatWithTemplateRequest,
  onMessage: (chunk: string) => void = () => {},
  onError: (error: Error) => void = () => {},
  onComplete: () => void = () => {},
): (() => void) => {
  const abortController = new AbortController();

  fetch("http://localhost:8001/api/agent/chat/template/stream", {
    method: "POST",
    headers: {
      ...getAuthHeaders(),
      Accept: "text/event-stream",
    },
    body: JSON.stringify(request),
    signal: abortController.signal,
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error("No response body");
      }

      const decoder = new TextDecoder();
      let buffer = "";

      while (true) {
        const { done, value } = await reader.read();
        if (done) {
          onComplete();
          break;
        }

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split("\n");
        buffer = lines.pop() || "";

        for (const line of lines) {
          if (line.startsWith("data:")) {
            const data = line.slice(5).trim();
            if (data) {
              onMessage(data);
            }
          }
        }
      }
    })
    .catch((error) => {
      if (error.name !== "AbortError") {
        onError(error);
      } else {
        onComplete();
      }
    });

  return () => {
    abortController.abort();
  };
};
