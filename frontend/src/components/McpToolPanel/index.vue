<script setup lang="ts">
defineOptions({ name: "McpToolPanel" });

import { ref, onMounted } from "vue";
import { ElMessage, ElButton, ElInput, ElTag, ElCollapse, ElCollapseItem } from "element-plus";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { getMCPTools, executeMCPTool, type MCPTool } from "@/api/mcpTool";

const tools = ref<MCPTool[]>([]);
const loading = ref(false);
const selectedTool = ref<MCPTool | null>(null);
const toolInput = ref("");
const executionResult = ref("");
const executing = ref(false);

const loadTools = async () => {
  loading.value = true;
  try {
    const response = await getMCPTools();
    if (response && Array.isArray(response)) {
      tools.value = response;
    } else if (response && response.tools) {
      tools.value = response.tools;
    }
  } catch (error: any) {
    console.error("Failed to load MCP tools:", error);
    ElMessage.error("加载工具列表失败: " + error.message);
  } finally {
    loading.value = false;
  }
};

const selectTool = (tool: MCPTool) => {
  selectedTool.value = tool;
  toolInput.value = "";
  executionResult.value = "";
};

const executeTool = async () => {
  if (!selectedTool.value) {
    ElMessage.warning("请先选择一个工具");
    return;
  }

  executing.value = true;
  executionResult.value = "";

  try {
    const args = parseInput(toolInput.value);
    const response = await executeMCPTool({
      toolName: selectedTool.value.name,
      arguments: args,
    });

    if (response.success !== false) {
      executionResult.value = response.result || response;
    } else {
      executionResult.value = "Error: " + (response.error || "Unknown error");
    }
  } catch (error: any) {
    executionResult.value = "Error: " + error.message;
  } finally {
    executing.value = false;
  }
};

const parseInput = (input: string): Record<string, any> => {
  try {
    return JSON.parse(input);
  } catch {
    return { input };
  }
};

const getToolIcon = (toolName: string): string => {
  if (toolName.includes("search")) return "ri:search-line";
  if (toolName.includes("calc") || toolName.includes("calculator")) return "ri:calculator-line";
  if (toolName.includes("file") && toolName.includes("read")) return "ri:file-read-line";
  if (toolName.includes("file") && toolName.includes("write")) return "ri:file-write-line";
  if (toolName.includes("http") || toolName.includes("request")) return "ri:global-line";
  return "ri:tools-line";
};

onMounted(() => {
  loadTools();
});
</script>

<template>
  <div class="mcp-tool-panel">
    <div class="panel-header">
      <span class="panel-title">MCP 工具</span>
      <el-button
        :icon="useRenderIcon('ep:refresh')"
        text
        @click="loadTools"
      />
    </div>

    <div class="tool-list">
      <div
        v-for="tool in tools"
        :key="tool.name"
        :class="['tool-item', { active: selectedTool?.name === tool.name }]"
        @click="selectTool(tool)"
      >
        <el-icon :size="16" class="tool-icon">
          <component :is="useRenderIcon(getToolIcon(tool.name))" />
        </el-icon>
        <div class="tool-info">
          <span class="tool-name">{{ tool.name }}</span>
          <span class="tool-desc">{{ tool.description }}</span>
        </div>
      </div>

      <div v-if="tools.length === 0 && !loading" class="empty-tools">
        <el-icon :size="32" color="var(--el-text-color-placeholder)">
          <component :is="useRenderIcon('ri:tools-line')" />
        </el-icon>
        <span>暂无可用工具</span>
      </div>
    </div>

    <div v-if="selectedTool" class="tool-executor">
      <div class="executor-header">
        <el-tag size="small">{{ selectedTool.name }}</el-tag>
      </div>

      <div class="input-area">
        <p class="input-hint">输入参数 (JSON 格式 或 纯文本)</p>
        <el-input
          v-model="toolInput"
          type="textarea"
          :rows="3"
          placeholder='{"query": "search term"} 或 直接输入搜索词'
        />
      </div>

      <div class="action-area">
        <el-button
          type="primary"
          :loading="executing"
          @click="executeTool"
        >
          执行
        </el-button>
      </div>

      <div v-if="executionResult" class="result-area">
        <p class="result-label">结果:</p>
        <pre class="result-content">{{ executionResult }}</pre>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.mcp-tool-panel {
  width: 280px;
  background: var(--el-fill-color-light);
  border-left: 1px solid var(--el-border-color-lighter);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.panel-title {
  font-weight: 500;
  font-size: 14px;
}

.tool-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.tool-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--el-fill-color);
  }

  &.active {
    background: var(--el-color-primary-light-9);
    border-left: 3px solid var(--el-color-primary);
  }
}

.tool-icon {
  flex-shrink: 0;
  margin-top: 2px;
  color: var(--el-text-color-secondary);
}

.tool-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden;
}

.tool-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.tool-desc {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-tools {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  gap: 8px;
  color: var(--el-text-color-placeholder);
  font-size: 13px;
}

.tool-executor {
  border-top: 1px solid var(--el-border-color-lighter);
  padding: 12px 16px;
  background: var(--el-bg-color);
}

.executor-header {
  margin-bottom: 10px;
}

.input-area {
  margin-bottom: 10px;

  .input-hint {
    font-size: 11px;
    color: var(--el-text-color-secondary);
    margin-bottom: 6px;
  }
}

.action-area {
  margin-bottom: 10px;

  .el-button {
    width: 100%;
  }
}

.result-area {
  .result-label {
    font-size: 11px;
    color: var(--el-text-color-secondary);
    margin-bottom: 6px;
  }

  .result-content {
    background: var(--el-fill-color);
    border-radius: 6px;
    padding: 10px;
    font-size: 12px;
    max-height: 150px;
    overflow-y: auto;
    white-space: pre-wrap;
    word-break: break-all;
    margin: 0;
  }
}
</style>
