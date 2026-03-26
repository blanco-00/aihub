import { defineStore } from "pinia";
import { store } from "@/store/utils";
import {
  getSessionList,
  getSessionMessages,
  createSession,
  deleteSession,
  updateSessionTitle,
  type ChatSession,
  type ChatMessage,
  type SessionListRequest,
  type PageResult,
} from "@/api/chatSession";
import { streamChat } from "@/api/streamChat";
import { ElMessage } from "element-plus";
import { getEnabledModelConfigs, type ModelConfig } from "@/api/modelConfig";
import { saveMessage } from "@/api/chatSession";

type ChatState = {
  sessions: ChatSession[];
  currentSessionId: number | null;
  currentSession: ChatSession | null;
  messages: ChatMessage[];
  loading: boolean;
  streaming: boolean;
  streamingContent: string;
  sessionTotal: number;
  sessionCurrent: number;
  sessionSize: number;
  searchKeyword: string;
};

export const useChatStore = defineStore("chat", {
  state: (): ChatState => ({
    sessions: [],
    currentSessionId: null,
    currentSession: null,
    messages: [],
    loading: false,
    streaming: false,
    streamingContent: "",
    sessionTotal: 0,
    sessionCurrent: 1,
    sessionSize: 20,
    searchKeyword: "",
  }),

  actions: {
    /**
     * 加载会话列表
     */
    async loadSessions(params?: Partial<SessionListRequest>) {
      this.loading = true;
      try {
        const requestParams: SessionListRequest = {
          keyword: params?.keyword || this.searchKeyword,
          current: params?.current || this.sessionCurrent,
          size: params?.size || this.sessionSize,
        };

        const response = await getSessionList(requestParams);

        if (response.code === 200 && response.data) {
          const pageData = response.data as PageResult<ChatSession>;
          this.sessions = pageData.records || [];
          this.sessionTotal = pageData.total || 0;
          this.sessionCurrent = pageData.current || 1;
          this.sessionSize = pageData.size || 20;
        } else {
          ElMessage.error(response.message || "加载会话列表失败");
        }
      } catch (error: any) {
        ElMessage.error("加载会话列表失败: " + error.message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * 选择会话
     */
    async selectSession(sessionId: number) {
      this.currentSessionId = sessionId;
      this.currentSession =
        this.sessions.find((s) => s.id === sessionId) || null;
      this.messages = [];
      this.streamingContent = "";

      await this.loadMessages(sessionId);
    },

    /**
     * 加载会话消息
     */
    async loadMessages(sessionId: number) {
      this.loading = true;
      try {
        const response = await getSessionMessages(sessionId);

        if (response.code === 200 && response.data) {
          this.messages = response.data;
        } else {
          ElMessage.error(response.message || "加载消息失败");
        }
      } catch (error: any) {
        ElMessage.error("加载消息失败: " + error.message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * 创建新会话
     */
    async createNewSession(
      modelId: number,
      title?: string,
    ): Promise<number | null> {
      try {
        const response = await createSession({
          modelId,
          title: title || "新会话",
        });

        if (response.code === 200 && response.data) {
          const sessionId = response.data;
          await this.loadSessions();
          return sessionId;
        } else {
          ElMessage.error(response.message || "创建会话失败");
          return null;
        }
      } catch (error: any) {
        ElMessage.error("创建会话失败: " + error.message);
        return null;
      }
    },

    /**
     * 删除会话
     */
    async removeSession(sessionId: number) {
      try {
        const response = await deleteSession(sessionId);

        if (response.code === 200) {
          await this.loadSessions();

          // 如果删除的是当前会话，清空消息
          if (this.currentSessionId === sessionId) {
            this.currentSessionId = null;
            this.currentSession = null;
            this.messages = [];
            this.streamingContent = "";
          }

          ElMessage.success("会话已删除");
        } else {
          ElMessage.error(response.message || "删除会话失败");
        }
      } catch (error: any) {
        ElMessage.error("删除会话失败: " + error.message);
      }
    },

    /**
     * 更新会话标题
     */
    async updateTitle(sessionId: number, title: string) {
      try {
        const response = await updateSessionTitle(sessionId, title);

        if (response.code === 200) {
          await this.loadSessions();
          ElMessage.success("标题已更新");
        } else {
          ElMessage.error(response.message || "更新标题失败");
        }
      } catch (error: any) {
        ElMessage.error("更新标题失败: " + error.message);
      }
    },

    async sendStreamMessage(
      modelName: string,
      content: string,
      sessionId?: string,
      skillIds?: number[],
    ): Promise<() => void> {
      const userMessage: ChatMessage = {
        id: Date.now(),
        sessionId: sessionId ? Number(sessionId) : 0,
        role: "user",
        content,
        createdAt: new Date().toISOString(),
      };
      this.messages.push(userMessage);

      if (sessionId) {
        saveMessage({
          sessionId: Number(sessionId),
          role: "user",
          content: content,
        }).catch((err) => console.error("Save user message failed:", err));
      }

      this.streaming = true;
      this.streamingContent = "";

      const cancel = streamChat({
        model: modelName,
        message: content,
        sessionId,
        onMessage: (chunk: string) => {
          this.streamingContent += chunk;
        },
        onError: (error: Error) => {
          this.streaming = false;
          ElMessage.error("发送消息失败: " + error.message);
        },
        onComplete: () => {
          const finalContent = this.streamingContent;
          
          if (finalContent && sessionId) {
            const assistantMessage: ChatMessage = {
              id: Date.now() + 1,
              sessionId: Number(sessionId),
              role: "assistant",
              content: finalContent,
              createdAt: new Date().toISOString(),
            };
            this.messages.push(assistantMessage);

            saveMessage({
              sessionId: Number(sessionId),
              role: "user",
              content: content,
            }).catch((err) => console.error("Save user message failed:", err));

            saveMessage({
              sessionId: Number(sessionId),
              role: "assistant",
              content: finalContent,
            }).catch((err) => console.error("Save assistant message failed:", err));
          }
          this.streaming = false;
          this.streamingContent = "";

          this.loadSessions();
        },
      });

      return cancel;
    },

    /**
     * 设置搜索关键词
     */
    setSearchKeyword(keyword: string) {
      this.searchKeyword = keyword;
    },

    /**
     * 清空当前会话
     */
    clearCurrentSession() {
      this.currentSessionId = null;
      this.currentSession = null;
      this.messages = [];
      this.streamingContent = "";
    },
  },
});

export function useChatStoreHook() {
  return useChatStore(store);
}
