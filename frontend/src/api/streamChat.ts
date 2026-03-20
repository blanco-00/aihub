import { getToken } from "@/utils/auth";

export type StreamChatOptions = {
  modelId: number;
  message: string;
  sessionId?: number;
  onMessage: (chunk: string) => void;
  onError: (error: Error) => void;
  onComplete: () => void;
};

/**
 * SSE 流式聊天 API
 * @param options 流式聊天选项
 * @returns 取消函数
 */
export const streamChat = (options: StreamChatOptions): (() => void) => {
  const token = getToken();
  const tokenStr = token?.accessToken ? `Bearer ${token.accessToken}` : "";

  // 构建 URL 参数
  const params = new URLSearchParams({
    modelId: String(options.modelId),
    message: options.message,
  });

  if (options.sessionId) {
    params.append("sessionId", String(options.sessionId));
  }

  // 使用 fetch + ReadableStream 实现 SSE
  const url = `/api/chat/stream?${params.toString()}`;

  let abortController: AbortController | null = new AbortController();

  fetch(url, {
    method: "GET",
    headers: {
      Accept: "text/event-stream",
      Authorization: tokenStr,
    },
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
          options.onComplete();
          break;
        }

        buffer += decoder.decode(value, { stream: true });

        // 处理 SSE 格式的数据
        const lines = buffer.split("\n");
        buffer = lines.pop() || ""; // 保留最后一个不完整的行

        for (const line of lines) {
          if (line.startsWith("data:")) {
            const data = line.slice(5).trim();
            if (data) {
              options.onMessage(data);
            }
          }
        }
      }
    })
    .catch((error) => {
      if (error.name === "AbortError") {
        // 用户主动取消，不算错误
        options.onComplete();
      } else {
        options.onError(error);
      }
    });

  // 返回取消函数
  return () => {
    if (abortController) {
      abortController.abort();
      abortController = null;
    }
  };
};
