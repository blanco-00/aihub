/**
 * RAG API - Python AI 服务知识检索
 * 提供文档管理、向量搜索、RAG聊天功能
 */
import { getToken } from "@/utils/auth";

// ============================================================
// 类型定义
// ============================================================

/** 文档状态 */
export type DocumentStatus = "pending" | "processing" | "ready" | "failed";

/** 文档信息 */
export type Document = {
  id: number;
  name: string;
  /** 文件大小(字节) */
  size: number;
  /** 文件类型: pdf, docx, txt, md */
  fileType: string;
  /** 文档状态 */
  status: DocumentStatus;
  /** 知识库ID */
  knowledgeBaseId: number;
  /** 知识库名称 */
  knowledgeBaseName?: string;
  /** 文档chunk数量 */
  chunkCount?: number;
  /** 处理错误信息 */
  errorMessage?: string;
  createdAt: string;
  updatedAt?: string;
};

/** 知识库信息 */
export type KnowledgeBase = {
  id: number;
  name: string;
  description: string;
  /** 文档数量 */
  documentCount: number;
  /** 状态 */
  status: number;
  createdAt: string;
  updatedAt?: string;
};

/** 创建知识库请求 */
export type CreateKnowledgeBaseRequest = {
  name: string;
  description?: string;
};

/** 上传文档请求 */
export type UploadDocumentRequest = {
  name: string;
  /** Base64编码的文件内容 */
  content: string;
  /** 文件类型 */
  fileType: string;
  /** 知识库ID */
  knowledgeBaseId: number;
};

/** 搜索结果 */
export type SearchResult = {
  /** 文档ID */
  documentId: number;
  /** 文档名称 */
  documentName: string;
  /** 匹配的chunk内容 */
  chunkContent: string;
  /** 相似度分数 */
  score: number;
  /** 所属知识库 */
  knowledgeBaseName: string;
};

/** RAG搜索请求 */
export type RAGSearchRequest = {
  /** 搜索query */
  query: string;
  /** 知识库ID列表（可选，空表示全部） */
  knowledgeBaseIds?: number[];
  /** 返回数量限制 */
  limit?: number;
};

/** RAG聊天请求 */
export type RAGChatRequest = {
  /** 用户问题 */
  query: string;
  /** 知识库ID列表（可选） */
  knowledgeBaseIds?: number[];
  /** 匹配chunk数量 */
  topK?: number;
  /** 会话ID */
  sessionId?: string;
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
 * 获取知识库列表
 */
export const getKnowledgeBases = async (): Promise<KnowledgeBase[]> => {
  const res = await fetch("http://localhost:8001/api/rag/knowledge-bases", {
    method: "GET",
    headers: getAuthHeaders(),
  });
  const data = await res.json();
  return data.data || [];
};

/**
 * 创建知识库
 */
export const createKnowledgeBase = async (
  kb: CreateKnowledgeBaseRequest,
): Promise<number> => {
  const res = await fetch("http://localhost:8001/api/rag/knowledge-bases", {
    method: "POST",
    headers: getAuthHeaders(),
    body: JSON.stringify(kb),
  });
  const data = await res.json();
  return data.data;
};

/**
 * 删除知识库
 */
export const deleteKnowledgeBase = async (id: number): Promise<void> => {
  await fetch(`http://localhost:8001/api/rag/knowledge-bases/${id}`, {
    method: "DELETE",
    headers: getAuthHeaders(),
  });
};

/**
 * 获取文档列表
 * @param knowledgeBaseId 知识库ID（可选）
 */
export const getDocuments = async (
  knowledgeBaseId?: number,
): Promise<Document[]> => {
  const url = new URL("http://localhost:8001/api/rag/documents");
  if (knowledgeBaseId) {
    url.searchParams.set("kb_id", String(knowledgeBaseId));
  }

  const res = await fetch(url.toString(), {
    method: "GET",
    headers: getAuthHeaders(),
  });
  const data = await res.json();
  return data.documents || [];
};

/**
 * 上传文档
 * @param doc 文档信息
 * @returns 新文档ID
 */
export const uploadDocument = async (
  doc: UploadDocumentRequest,
): Promise<number> => {
  const res = await fetch("http://localhost:8001/api/rag/documents", {
    method: "POST",
    headers: getAuthHeaders(),
    body: JSON.stringify(doc),
  });
  const data = await res.json();
  return data.data;
};

/**
 * 删除文档
 */
export const deleteDocument = async (id: number): Promise<void> => {
  await fetch(`http://localhost:8001/api/rag/documents/${id}`, {
    method: "DELETE",
    headers: getAuthHeaders(),
  });
};

/**
 * 获取文档处理状态
 */
export const getDocumentStatus = async (
  id: number,
): Promise<DocumentStatus> => {
  const res = await fetch(
    `http://localhost:8001/api/rag/documents/${id}/status`,
    {
      method: "GET",
      headers: getAuthHeaders(),
    },
  );
  const data = await res.json();
  return data.data?.status || "pending";
};

/**
 * 搜索文档内容
 */
export const searchDocuments = async (
  request: RAGSearchRequest,
): Promise<SearchResult[]> => {
  const url = new URL("http://localhost:8001/api/rag/search");
  url.searchParams.set("q", request.query);
  if (request.knowledgeBaseIds?.length) {
    url.searchParams.set("kb_ids", request.knowledgeBaseIds.join(","));
  }
  if (request.limit) {
    url.searchParams.set("top_k", String(request.limit));
  }

  const res = await fetch(url.toString(), {
    method: "GET",
    headers: getAuthHeaders(),
  });
  const data = await res.json();
  return data.results || [];
};

/**
 * RAG聊天 - 标准响应
 */
export const ragChat = async (
  request: RAGChatRequest,
): Promise<{ answer: string; sources: SearchResult[] }> => {
  const res = await fetch("http://localhost:8001/api/rag/chat", {
    method: "POST",
    headers: getAuthHeaders(),
    body: JSON.stringify(request),
  });
  const data = await res.json();
  return data.data || { answer: "", sources: [] };
};

/**
 * RAG聊天 - 流式响应
 */
export const ragChatStream = (
  request: RAGChatRequest,
  onMessage: (chunk: string) => void = () => {},
  onError: (error: Error) => void = () => {},
  onComplete: () => void = () => {},
): (() => void) => {
  const abortController = new AbortController();

  fetch("http://localhost:8001/api/rag/chat/stream", {
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
