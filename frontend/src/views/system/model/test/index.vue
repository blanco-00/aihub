<script setup lang="ts">
defineOptions({
  name: "SystemModelTest",
});

import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { PureTableBar } from "@/components/RePureTableBar";
import {
  getEnabledModelConfigs,
  chatWithModel,
  checkModelHealth,
  getTestHistory,
  type ModelTestResult,
} from "@/api/modelConfig";

const tableRef = ref();
const chatBoxRef = ref();
const inputRef = ref();
const loading = ref(false);
const currentMessageId = ref<number | null>(null);
const messages = ref<
  Array<{
    id: number;
    role: "user" | "model";
    content: string;
    timestamp: string;
  }>
>([]);

const selectedModelId = ref<number | null>(null);
const selectedModelName = ref<string>("");
const testHistory = ref<
  Array<{
    modelId: number;
    modelName: string;
    userMessage: string;
    modelResponse: string | null;
    responseTimeMs: number;
    timestamp: string;
    success: boolean;
    error: string | null;
  }>
>([]);

const sendMessage = async () => {
  const message = inputRef.value?.trim();
  if (!message) {
    ElMessage.warning("请输入消息内容");
    return;
  }

  if (!selectedModelId.value) {
    ElMessage.warning("请先选择一个模型");
    return;
  }

  loading.value = true;
  try {
    currentMessageId.value = Date.now();
    const response = await chatWithModel(selectedModelId.value, message);

    // Add user message
    messages.value.push({
      id: currentMessageId.value,
      role: "user",
      content: message,
      timestamp: new Date().toISOString(),
    });

    // Add model response
    if (response.code === 200 && response.data) {
      // 后端返回 Result.success(response)，data 已经是实际响应内容
      messages.value.push({
        id: currentMessageId.value + 1,
        role: "model",
        content: response.data,
        timestamp: new Date().toISOString(),
      });

      currentMessageId.value = currentMessageId.value + 2;
    } else if (response.code !== 200) {
      messages.value.push({
        id: currentMessageId.value + 1,
        role: "model",
        content: `[错误] ${response.message || "未知错误"}`,
        timestamp: new Date().toISOString(),
      });
    }

    // Scroll to bottom
    setTimeout(() => {
      chatBoxRef.value?.scrollTo({ bottom: 0, behavior: "smooth" });
    }, 100);
  } catch (error: any) {
    ElMessage.error("发送消息失败: " + error.message);
  } finally {
    loading.value = false;
  }
};

const handleModelChange = async () => {
  const modelId = selectedModelId.value;
  if (!modelId) return;

  loading.value = true;
  try {
    const health = await checkModelHealth(modelId);

    if (health.code === 200 && health.data === true) {
      ElMessage.success(`模型 "${selectedModelName.value}" 可用`);
    } else {
      ElMessage.warning(
        `模型 "${selectedModelName.value}" 不可用: ${health.message || "未知错误"}`,
      );
      selectedModelId.value = null;
      selectedModelName.value = "";
    }
  } catch (error: any) {
    ElMessage.error("检查模型状态失败: " + error.message);
  } finally {
    loading.value = false;
  }
};

const loadTestHistory = async () => {
  loading.value = true;
  try {
    const response = await getTestHistory({ modelId: selectedModelId.value });

    if (response.code === 200 && response.data) {
      testHistory.value = response.data.records || [];
    } else {
      ElMessage.error("加载测试历史失败");
      testHistory.value = [];
    }
  } catch (error: any) {
    ElMessage.error("加载测试历史失败: " + error.message);
  } finally {
    loading.value = false;
  }
};

const clearChat = () => {
  messages.value = [];
  ElMessage.success("对话已清空");
};

onMounted(async () => {
  // Load available models
  loading.value = true;
  try {
    const response = await getEnabledModelConfigs();

    if (response.code === 200 && response.data) {
      if (response.data.length > 0) {
        selectedModelId.value = response.data[0].id;
        selectedModelName.value = response.data[0].name;
      }
    } else {
      ElMessage.warning("暂无可用的模型，请先配置模型");
    }
  } catch (error: any) {
    ElMessage.error("加载模型列表失败: " + error.message);
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="model-test-container">
    <!-- 模型选择和操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-select
          v-model="selectedModelId"
          placeholder="请选择模型"
          :loading="loading"
          style="width: 300px; margin-right: 16px"
          @change="handleModelChange"
        >
          <el-option
            v-for="model in getEnabledModelConfigs.data || []"
            :key="model.id"
            :label="`${model.name} (${model.vendor})`"
            :value="model.id"
          />
        </el-select>

        <el-button
          :icon="useRenderIcon(Refresh)"
          :loading="loading"
          style="margin-right: 8px"
          @click="loadTestHistory"
        >
          刷新历史
        </el-button>

        <el-button
          type="danger"
          :icon="useRenderIcon('ri-delete-bin-line')"
          :disabled="!selectedModelId"
          @click="clearChat"
        >
          清空对话
        </el-button>
      </div>
    </div>

    <!-- 聊天界面 -->
    <div ref="chatBoxRef" class="chat-container">
      <!-- 消息列表 -->
      <div class="messages">
        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="[
            'message',
            msg.role === 'user' ? 'user-message' : 'model-message',
          ]"
        >
          <div class="message-content">
            <div class="message-header">
              <span class="message-role">{{
                msg.role === "user" ? "用户" : "模型"
              }}</span>
              <span class="message-time">{{
                new Date(msg.timestamp).toLocaleTimeString()
              }}</span>
            </div>
            <div class="message-text">{{ msg.content }}</div>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="input-area">
        <el-input
          ref="inputRef"
          v-model="inputRef"
          type="textarea"
          :rows="4"
          placeholder="请输入消息内容..."
          :disabled="loading"
          class="message-input"
          @keydown.enter.exact="sendMessage"
        />
        <el-button
          type="primary"
          :icon="useRenderIcon('ri-send-plane-fill')"
          :loading="loading"
          :disabled="!selectedModelId || loading"
          style="margin-top: 16px"
          @click="sendMessage"
        >
          发送
        </el-button>
      </div>

      <!-- 模型信息 -->
      <div v-if="selectedModelName" class="model-info">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>当前模型</span>
              <span>{{ selectedModelName }}</span>
            </div>
          </template>
          <div class="card-body">
            <el-tag
              v-if="selectedModelId"
              type="info"
              style="margin-bottom: 8px"
            >
              模型可用
            </el-tag>
            <div class="info-item">
              <span class="info-label">模型 ID:</span>
              <span>{{
                (getEnabledModelConfigs.data || []).find(
                  (m) => m.id === selectedModelId.value,
                )?.modelId || "N/A"
              }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">厂商:</span>
              <span>{{
                (getEnabledModelConfigs.data || []).find(
                  (m) => m.id === selectedModelId.value,
                )?.vendor || "N/A"
              }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">状态:</span>
              <el-tag
                :type="
                  checkModelHealth.data && checkModelHealth.data
                    ? 'success'
                    : 'warning'
                "
                style="margin-left: 8px"
              >
                {{
                  checkModelHealth.data && checkModelHealth.data
                    ? "可用"
                    : "不可用"
                }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.model-test-container {
  height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  padding: 12px;
  background: var(--el-bg-color-page);
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: var(--el-box-shadow-light);
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  max-height: calc(100vh - 200px);
}

.message {
  margin-bottom: 16px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.message-role {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  padding: 2px 6px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
}

.message-time {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}

.message-text {
  padding: 12px 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.user-message {
  background: var(--el-color-primary-light);
  border-left: 3px solid var(--el-color-primary);
}

.model-message {
  background: var(--el-fill-color-light);
  border-left: 3px solid var(--el-color-success);
}

.input-area {
  padding: 12px 16px;
  background: var(--el-fill-color-light);
  border-radius: 0 0 8px 8px 8px 0;
}

.message-input {
  flex: 1;
  gap: 8px;
}

.message-input :deep(.el-textarea__inner) {
  font-family: inherit;
}

.model-info {
  flex: 0;
  padding: 12px;
  background: var(--el-fill-color-light);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.info-label {
  color: var(--el-text-color-secondary);
  font-weight: 500;
}
</style>
