/**
 * Skills API - Python AI 服务技能管理
 * 提供技能的CRUD、关键词检测、聊天功能
 */
import { getToken } from "@/utils/auth";

// ============================================================
// 类型定义
// ============================================================

/** 技能信息 */
export type Skill = {
  id: number;
  name: string;
  description: string;
  /** 触发关键词列表 */
  keywords: string[];
  /** 是否内置 */
  is_builtin?: boolean;
  /** 关键词匹配模式: exact(精确) | fuzzy(模糊) | both */
  matchMode?: "exact" | "fuzzy" | "both";
  /** 关联的Agent模板ID */
  templateId?: number;
  /** 系统提示词 */
  systemPrompt?: string;
  /** 状态: 0-禁用, 1-启用 */
  status?: number;
  createdAt?: string;
  updatedAt?: string;
};

/** 创建技能请求 */
export type CreateSkillRequest = {
  name: string;
  description: string;
  keywords: string[];
  matchMode?: "exact" | "fuzzy" | "both";
  templateId?: number;
  systemPrompt?: string;
};

/** 更新技能请求 */
export type UpdateSkillRequest = {
  id: number;
  name: string;
  description: string;
  keywords: string[];
  matchMode?: "exact" | "fuzzy" | "both";
  templateId?: number;
  systemPrompt?: string;
  status: number;
};

/** 技能检测结果 */
export type SkillDetectionResult = {
  matched: boolean;
  matchedSkills: Skill[];
  confidence: number;
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
 * 获取技能列表
 * @returns 技能列表
 */
export const getSkills = async (): Promise<Skill[]> => {
  const res = await fetch("http://localhost:8001/api/skills", {
    method: "GET",
    headers: getAuthHeaders(),
  });
  const data = await res.json();
  return data.skills || [];
};

/**
 * 获取单个技能
 * @param id 技能ID
 * @returns 技能详情
 */
export const getSkill = async (id: number): Promise<Skill | null> => {
  const res = await fetch(`http://localhost:8001/api/skills/${id}`, {
    method: "GET",
    headers: getAuthHeaders(),
  });
  const data = await res.json();
  return data.skill || null;
};

/**
 * 创建技能
 * @param skill 技能信息
 * @returns 新技能ID
 */
export const createSkill = async (
  skill: CreateSkillRequest,
): Promise<number> => {
  const res = await fetch("http://localhost:8001/api/skills", {
    method: "POST",
    headers: getAuthHeaders(),
    body: JSON.stringify(skill),
  });
  const data = await res.json();
  return data.data;
};

/**
 * 更新技能
 * @param skill 技能信息
 */
export const updateSkill = async (skill: UpdateSkillRequest): Promise<void> => {
  await fetch(`http://localhost:8001/api/skills/${skill.id}`, {
    method: "PUT",
    headers: getAuthHeaders(),
    body: JSON.stringify(skill),
  });
};

/**
 * 删除技能
 * @param id 技能ID
 */
export const deleteSkill = async (id: number): Promise<void> => {
  await fetch(`http://localhost:8001/api/skills/${id}`, {
    method: "DELETE",
    headers: getAuthHeaders(),
  });
};

/**
 * 检测消息中包含的技能
 * @param message 用户消息
 * @returns 匹配到的技能列表
 */
export const detectSkills = async (
  message: string,
): Promise<SkillDetectionResult> => {
  const res = await fetch("http://localhost:8001/api/skills/detect", {
    method: "POST",
    headers: getAuthHeaders(),
    body: JSON.stringify({ message }),
  });
  const data = await res.json();
  return data.data || { matched: false, matchedSkills: [], confidence: 0 };
};

/**
 * 使用技能进行聊天
 * @param skillId 技能ID
 * @param message 用户消息
 * @param sessionId 会话ID（可选）
 * @param onMessage 流式消息回调
 * @param onError 错误回调
 * @param onComplete 完成回调
 * @returns 取消函数
 */
export const chatWithSkill = (
  skillId: number,
  message: string,
  sessionId?: string,
  onMessage: (chunk: string) => void = () => {},
  onError: (error: Error) => void = () => {},
  onComplete: () => void = () => {},
): (() => void) => {
  const abortController = new AbortController();

  fetch(`http://localhost:8001/api/skills/chat/${skillId}`, {
    method: "POST",
    headers: {
      ...getAuthHeaders(),
      Accept: "text/event-stream",
    },
    body: JSON.stringify({ message, session_id: sessionId }),
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
