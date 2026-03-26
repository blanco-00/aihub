import { getToken } from "@/utils/auth";

export type StreamChatOptions = {
  model: string;
  message: string;
  sessionId?: string;
  onMessage: (chunk: string) => void;
  onError: (error: Error) => void;
  onComplete: () => void;
};

export const streamChat = (options: StreamChatOptions): (() => void) => {
  const token = getToken();
  const tokenStr = token?.accessToken ? `Bearer ${token.accessToken}` : "";

  const url = "http://localhost:8001/api/agent/chat/stream";

  let abortController: AbortController | null = new AbortController();

  fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "text/event-stream",
      Authorization: tokenStr,
    },
    body: JSON.stringify({
      model: options.model,
      message: options.message,
      session_id: options.sessionId,
    }),
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

        const lines = buffer.split("\n");
        buffer = lines.pop() || "";

        for (const line of lines) {
          if (line.startsWith("data:")) {
            const data = line.slice(5).trim();
            if (!data || data === "[DONE]") {
              continue;
            }
            try {
              // 解析 JSON 格式的 SSE 数据
              const parsed = JSON.parse(data);
              if (parsed.content) {
                options.onMessage(parsed.content);
              }
            } catch {
              // 如果不是 JSON，直接发送（兼容纯文本格式）
              options.onMessage(data);
            }
          }
        }
      }
    })
    .catch((error) => {
      if (error.name === "AbortError") {
        options.onComplete();
      } else {
        options.onError(error);
      }
    });

  return () => {
    if (abortController) {
      abortController.abort();
      abortController = null;
    }
  };
};
