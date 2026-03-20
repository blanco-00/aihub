<script setup lang="ts">
defineOptions({
  name: "AiChat",
});

import { ref, onMounted, nextTick, computed, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { useChatStore } from "@/store/modules/chat";
import { storeToRefs } from "pinia";
import {
  getEnabledModelConfigs,
  checkModelHealth,
  type ModelConfig,
} from "@/api/modelConfig";
import StreamMessage from "@/components/StreamMessage/index.vue";

const chatStore = useChatStore();
const {
  sessions,
  currentSessionId,
  currentSession,
  messages,
  loading,
  streaming,
  streamingContent,
  sessionTotal,
  sessionCurrent,
  searchKeyword,
} = storeToRefs(chatStore);

const inputMessage = ref("");
const messageContainerRef = ref<HTMLElement>();
const sessionListRef = ref<HTMLElement>();
const modelList = ref<ModelConfig[]>([]);
const selectedModelId = ref<number | null>(null);
const selectedModel = ref<ModelConfig | null>(null);
const modelHealthy = ref<boolean | null>(null);
const cancelStreamRef = ref<(() => void) | null>(null);
const sessionSearchKeyword = ref("");
const isCollapsed = ref(false);

const vendorMap: Record<string, string> = {
  openai: "OpenAI",
  anthropic: "Anthropic",
  azure: "Azure",
  baidu: "百度",
  ali: "阿里",
  tencent: "腾讯",
  zhipuai: "智谱",
};

// 合并消息列表（包含流式内容）
const displayMessages = computed(() => {
  const msgs = [...messages.value];
  if (streaming.value && streamingContent.value) {
    msgs.push({
      id: Date.now(),
      sessionId: currentSessionId.value || 0,
      role: "assistant" as const,
      content: streamingContent.value,
      createdAt: new Date().toISOString(),
    });
  }
  return msgs;
});

// 发送消息
const sendMessage = async () => {
  const content = inputMessage.value.trim();
  if (!content) {
    ElMessage.warning("请输入消息内容");
    return;
  }

  if (!selectedModelId.value) {
    ElMessage.warning("请先选择一个模型");
    return;
  }

  inputMessage.value = "";

  try {
    // 如果没有当前会话，先创建一个
    let sessionId = currentSessionId.value;
    if (!sessionId) {
      sessionId = await chatStore.createNewSession(
        selectedModelId.value,
        content.slice(0, 30) + (content.length > 30 ? "..." : ""),
      );
      if (!sessionId) {
        ElMessage.error("创建会话失败");
        return;
      }
      await chatStore.selectSession(sessionId);
    }

    // 发送流式消息
    cancelStreamRef.value = await chatStore.sendStreamMessage(
      selectedModelId.value,
      content,
      sessionId,
    );

    await nextTick();
    scrollToBottom();
  } catch (error: any) {
    ElMessage.error("发送消息失败: " + error.message);
  }
};

// 停止流式响应
const stopStreaming = () => {
  if (cancelStreamRef.value) {
    cancelStreamRef.value();
    cancelStreamRef.value = null;
  }
};

// 滚动到底部
const scrollToBottom = () => {
  if (messageContainerRef.value) {
    messageContainerRef.value.scrollTop =
      messageContainerRef.value.scrollHeight;
  }
};

// 键盘事件
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === "Enter" && !e.shiftKey) {
    e.preventDefault();
    if (!streaming.value) {
      sendMessage();
    }
  }
};

// 清空当前会话
const clearCurrentChat = () => {
  ElMessageBox.confirm("确定要清空当前对话吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    chatStore.clearCurrentSession();
    ElMessage.success("对话已清空");
  });
};

// 新建会话
const createNewChat = async () => {
  if (!selectedModelId.value) {
    ElMessage.warning("请先选择一个模型");
    return;
  }

  const sessionId = await chatStore.createNewSession(selectedModelId.value);
  if (sessionId) {
    await chatStore.selectSession(sessionId);
    ElMessage.success("新会话已创建");
  }
};

// 删除会话
const deleteChatSession = (sessionId: number, event: Event) => {
  event.stopPropagation();
  ElMessageBox.confirm("确定要删除这个会话吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    chatStore.removeSession(sessionId);
  });
};

// 选择会话
const selectChatSession = async (sessionId: number) => {
  await chatStore.selectSession(sessionId);
  scrollToBottom();
};

// 搜索会话
const handleSearch = () => {
  chatStore.setSearchKeyword(sessionSearchKeyword.value);
  chatStore.loadSessions({ current: 1 });
};

// 导出会话
const exportCurrentSession = () => {
  if (!currentSessionId.value) {
    ElMessage.warning("请先选择一个会话");
    return;
  }
  window.open(`/api/chat/session/${currentSessionId.value}/export`, "_blank");
};

// 加载模型列表
const loadModels = async () => {
  try {
    const response = await getEnabledModelConfigs();
    if (response.code === 200 && response.data) {
      modelList.value = response.data;
      if (response.data.length > 0) {
        selectedModelId.value = response.data[0].id;
        selectedModel.value = response.data[0];
        modelHealthy.value = true;
      }
    }
  } catch (error: any) {
    ElMessage.error("加载模型列表失败: " + error.message);
  }
};

// 模型切换（仅手动切换时检查）
const handleModelChange = async () => {
  selectedModel.value =
    modelList.value.find((m) => m.id === selectedModelId.value) || null;

  if (!selectedModelId.value) {
    modelHealthy.value = null;
    return;
  }

  modelHealthy.value = null;

  try {
    const result = await checkModelHealth(selectedModelId.value);
    modelHealthy.value = result.code === 200 && result.data === true;
  } catch {
    modelHealthy.value = false;
  }
};

// 切换侧边栏
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

// 格式化时间
const formatTime = (dateStr: string) => {
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days === 0) {
    return date.toLocaleTimeString("zh-CN", {
      hour: "2-digit",
      minute: "2-digit",
    });
  } else if (days === 1) {
    return "昨天";
  } else if (days < 7) {
    return `${days}天前`;
  } else {
    return date.toLocaleDateString("zh-CN", {
      month: "2-digit",
      day: "2-digit",
    });
  }
};

// 监听搜索关键词
watch(sessionSearchKeyword, () => {
  if (sessionSearchKeyword.value === "") {
    handleSearch();
  }
});

onMounted(() => {
  loadModels();
  chatStore.loadSessions();
});
</script>

<template>
  <div class="ai-chat-container">
    <!-- 左侧会话列表 -->
    <div class="session-sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <el-button
          v-if="!isCollapsed"
          type="primary"
          :icon="useRenderIcon('ep:plus')"
          style="width: 100%"
          @click="createNewChat"
        >
          新建会话
        </el-button>
        <el-button
          v-else
          type="primary"
          :icon="useRenderIcon('ep:plus')"
          circle
          @click="createNewChat"
        />
      </div>

      <div v-if="!isCollapsed" class="session-search">
        <el-input
          v-model="sessionSearchKeyword"
          placeholder="搜索会话"
          :prefix-icon="useRenderIcon('ep:search')"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
      </div>

      <div v-if="!isCollapsed" ref="sessionListRef" class="session-list">
        <div
          v-for="session in sessions"
          :key="session.id"
          :class="['session-item', { active: currentSessionId === session.id }]"
          @click="selectChatSession(session.id)"
        >
          <div class="session-info">
            <div class="session-title">{{ session.title }}</div>
            <div class="session-meta">
              <span class="session-model">{{ session.modelName }}</span>
              <span class="session-time">
                {{ formatTime(session.lastMessageAt || session.createdAt) }}
              </span>
            </div>
          </div>
          <el-button
            text
            size="small"
            :icon="useRenderIcon('ep:delete')"
            class="session-delete"
            @click="deleteChatSession(session.id, $event)"
          />
        </div>

        <div v-if="sessions.length === 0" class="empty-sessions">
          <el-icon :size="40" color="var(--el-text-color-placeholder)">
            <component :is="useRenderIcon('ri:chat-voice-line')" />
          </el-icon>
          <p>暂无会话记录</p>
        </div>
      </div>

      <div class="sidebar-toggle" @click="toggleSidebar">
        <el-icon :size="16">
          <component
            :is="
              useRenderIcon(
                isCollapsed ? 'ep:d-arrow-right' : 'ep:d-arrow-left',
              )
            "
          />
        </el-icon>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-main">
      <!-- 顶部工具栏 -->
      <div class="chat-header">
        <div class="model-selector">
          <el-select
            v-model="selectedModelId"
            placeholder="选择模型"
            :loading="loading"
            style="width: 280px"
            @change="handleModelChange"
          >
            <el-option
              v-for="model in modelList"
              :key="model.id"
              :label="`${model.name} (${vendorMap[model.vendor] || model.vendor})`"
              :value="model.id"
            />
          </el-select>
          <el-tag
            v-if="selectedModel && modelHealthy !== null"
            :type="modelHealthy ? 'success' : 'danger'"
            style="margin-left: 12px"
          >
            {{ modelHealthy ? "可用" : "不可用" }}
          </el-tag>
          <el-tag
            v-else-if="selectedModel && modelHealthy === null"
            type="warning"
            style="margin-left: 12px"
          >
            检查中...
          </el-tag>
        </div>
        <div class="header-actions">
          <el-button
            v-if="currentSessionId"
            :icon="useRenderIcon('ep:download')"
            @click="exportCurrentSession"
          >
            导出
          </el-button>
          <el-button
            :icon="useRenderIcon('ep:delete')"
            :disabled="messages.length === 0"
            @click="clearCurrentChat"
          >
            清空
          </el-button>
        </div>
      </div>

      <!-- 消息区域 -->
      <div ref="messageContainerRef" class="chat-messages">
        <div v-if="messages.length === 0 && !streaming" class="empty-state">
          <el-icon :size="64" color="var(--el-text-color-placeholder)">
            <component :is="useRenderIcon('ri:chat-voice-line')" />
          </el-icon>
          <p>选择一个会话或创建新会话开始对话</p>
        </div>

        <div
          v-for="msg in displayMessages"
          :key="msg.id"
          :class="['message-wrapper', msg.role]"
        >
          <div class="message-avatar">
            <el-avatar
              :size="36"
              :icon="
                msg.role === 'user'
                  ? useRenderIcon('ep:user')
                  : useRenderIcon('ri:robot-2-line')
              "
            />
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="message-role">
                {{ msg.role === "user" ? "你" : selectedModel?.name || "AI" }}
              </span>
              <span class="message-time">
                {{ new Date(msg.createdAt).toLocaleTimeString() }}
              </span>
            </div>
            <div class="message-bubble">
              <StreamMessage
                :content="msg.content"
                :isStreaming="
                  streaming &&
                  msg.id === displayMessages[displayMessages.length - 1]?.id &&
                  msg.role === 'assistant'
                "
                :role="msg.role"
              />
            </div>
          </div>
        </div>

        <div v-if="loading" class="loading-indicator">
          <el-icon class="is-loading" :size="20">
            <component :is="useRenderIcon('ep:loading')" />
          </el-icon>
          <span>加载中...</span>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="输入消息，Enter 发送，Shift+Enter 换行"
          :disabled="loading || streaming || !selectedModelId"
          resize="none"
          @keydown="handleKeydown"
        />
        <div class="input-actions">
          <el-button
            v-if="streaming"
            type="danger"
            :icon="useRenderIcon('ep:video-pause')"
            @click="stopStreaming"
          >
            停止
          </el-button>
          <el-button
            v-else
            type="primary"
            :icon="useRenderIcon('ri:send-plane-fill')"
            :loading="loading"
            :disabled="!selectedModelId || !inputMessage.trim()"
            @click="sendMessage"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.ai-chat-container {
  display: flex;
  height: calc(100vh - 84px);
  background: var(--el-bg-color);
}

.session-sidebar {
  width: 280px;
  background: var(--el-fill-color-light);
  border-right: 1px solid var(--el-border-color-lighter);
  display: flex;
  flex-direction: column;
  transition: width 0.3s;

  &.collapsed {
    width: 60px;

    .sidebar-header {
      justify-content: center;
      padding: 12px;
    }
  }
}

.sidebar-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.session-search {
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  margin-bottom: 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background: var(--el-fill-color);

    .session-delete {
      display: block;
    }
  }

  &.active {
    background: var(--el-color-primary-light-9);
    border-left: 3px solid var(--el-color-primary);
  }
}

.session-info {
  flex: 1;
  overflow: hidden;
}

.session-title {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.session-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.session-model {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  flex-shrink: 0;
  margin-left: 8px;
}

.session-delete {
  display: none;
  margin-left: 8px;
}

.empty-sessions {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--el-text-color-placeholder);

  p {
    margin-top: 12px;
    font-size: 14px;
  }
}

.sidebar-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background: var(--el-fill-color);
  }
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.model-selector {
  display: flex;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--el-text-color-placeholder);

  p {
    margin-top: 16px;
    font-size: 14px;
  }
}

.message-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  &.user {
    flex-direction: row-reverse;

    .message-content {
      align-items: flex-end;
    }

    .message-bubble {
      background: var(--el-color-primary-light-9);
      border-right: 3px solid var(--el-color-primary);
    }
  }

  &.assistant {
    .message-bubble {
      background: var(--el-fill-color-light);
      border-left: 3px solid var(--el-color-success);
    }
  }
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 12px;
}

.message-role {
  color: var(--el-text-color-secondary);
}

.message-time {
  color: var(--el-text-color-placeholder);
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.chat-input {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: var(--el-fill-color-light);
  border-top: 1px solid var(--el-border-color-lighter);

  .el-textarea {
    flex: 1;
  }

  .input-actions {
    display: flex;
    align-items: flex-end;
  }
}
</style>
