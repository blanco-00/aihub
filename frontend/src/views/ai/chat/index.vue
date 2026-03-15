<script setup lang="ts">
defineOptions({
  name: "AiChat"
});

import { ref, onMounted, nextTick, computed } from "vue";
import { ElMessage } from "element-plus";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import {
  getEnabledModelConfigs,
  chatWithModel,
  checkModelHealth,
  type ModelConfig
} from "@/api/modelConfig";

interface Message {
  id: number;
  role: "user" | "model";
  content: string;
  timestamp: string;
}

const loading = ref(false);
const inputMessage = ref("");
const messages = ref<Message[]>([]);
const messageContainerRef = ref<HTMLElement>();
const modelList = ref<ModelConfig[]>([]);
const selectedModelId = ref<number | null>(null);
const selectedModel = ref<ModelConfig | null>(null);
const modelHealthy = ref<boolean | null>(null); // null 表示未检查

const vendorMap: Record<string, string> = {
  openai: "OpenAI",
  anthropic: "Anthropic",
  azure: "Azure",
  baidu: "百度",
  ali: "阿里",
  tencent: "腾讯",
  zhipuai: "智谱"
};

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

  loading.value = true;
  const userMsgId = Date.now();

  messages.value.push({
    id: userMsgId,
    role: "user",
    content,
    timestamp: new Date().toISOString()
  });

  inputMessage.value = "";

  await nextTick();
  scrollToBottom();

  try {
    const response = await chatWithModel(selectedModelId.value!, content);

    if (response.code === 200 && response.data) {
      // 后端返回 Result.success(response)，data 已经是实际响应内容
      const modelContent = response.data;
      messages.value.push({
        id: userMsgId + 1,
        role: "model",
        content: modelContent,
        timestamp: new Date().toISOString()
      });
    } else {
      messages.value.push({
        id: userMsgId + 1,
        role: "model",
        content: `[错误] ${response.message || "未知错误"}`,
        timestamp: new Date().toISOString()
      });
    }
  } catch (error: any) {
    messages.value.push({
      id: userMsgId + 1,
      role: "model",
      content: `[错误] ${error.message || "请求失败"}`,
      timestamp: new Date().toISOString()
    });
  } finally {
    loading.value = false;
    await nextTick();
    scrollToBottom();
  }
};

const scrollToBottom = () => {
  if (messageContainerRef.value) {
    messageContainerRef.value.scrollTop = messageContainerRef.value.scrollHeight;
  }
};

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === "Enter" && !e.shiftKey) {
    e.preventDefault();
    sendMessage();
  }
};

const clearChat = () => {
  messages.value = [];
  ElMessage.success("对话已清空");
};

const handleModelChange = async () => {
  selectedModel.value =
    modelList.value.find(m => m.id === selectedModelId.value) || null;
  
  if (!selectedModelId.value) {
    modelHealthy.value = null;
    return;
  }
  
  // 开始检查时设置为 null，表示正在检查
  modelHealthy.value = null;
  
  try {
    const result = await checkModelHealth(selectedModelId.value);
    modelHealthy.value = result.code === 200 && result.data === true;
  } catch {
    modelHealthy.value = false;
  }
};

const loadModels = async () => {
  try {
    const response = await getEnabledModelConfigs();
    if (response.code === 200 && response.data) {
      modelList.value = response.data;
      if (response.data.length > 0) {
        selectedModelId.value = response.data[0].id;
        selectedModel.value = response.data[0];
        handleModelChange();
      }
    }
  } catch (error: any) {
    ElMessage.error("加载模型列表失败: " + error.message);
  }
};

onMounted(() => {
  loadModels();
});
</script>

<template>
  <div class="ai-chat-container">
    <div class="chat-header">
      <div class="model-selector">
        <el-select
          v-model="selectedModelId"
          placeholder="选择模型"
          :loading="loading"
          @change="handleModelChange"
          style="width: 280px"
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
          :icon="useRenderIcon('ep:delete')"
          @click="clearChat"
          :disabled="messages.length === 0"
        >
          清空对话
        </el-button>
      </div>
    </div>

    <div class="chat-messages" ref="messageContainerRef">
      <div v-if="messages.length === 0" class="empty-state">
        <el-icon :size="64" color="var(--el-text-color-placeholder)">
          <component :is="useRenderIcon('ri:chat-voice-line')" />
        </el-icon>
        <p>选择一个模型开始对话</p>
      </div>

      <div
        v-for="msg in messages"
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
              {{ new Date(msg.timestamp).toLocaleTimeString() }}
            </span>
          </div>
          <div class="message-bubble">
            <pre>{{ msg.content }}</pre>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading-indicator">
        <el-icon class="is-loading" :size="20">
          <component :is="useRenderIcon('ep:loading')" />
        </el-icon>
        <span>AI 正在思考...</span>
      </div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="3"
        placeholder="输入消息，Enter 发送，Shift+Enter 换行"
        :disabled="loading || !selectedModelId"
        @keydown="handleKeydown"
        resize="none"
      />
      <el-button
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
</template>

<style lang="scss" scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 84px);
  background: var(--el-bg-color);
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

  &.model {
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

  pre {
    margin: 0;
    white-space: pre-wrap;
    word-wrap: break-word;
    font-family: inherit;
    line-height: 1.6;
  }
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

  .el-button {
    align-self: flex-end;
  }
}
</style>
