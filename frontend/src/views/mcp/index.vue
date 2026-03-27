<script setup lang="ts">
defineOptions({ name: "McpIndex" });

import { ref, onMounted } from "vue";
import { ElMessage, ElButton, ElInput, ElTag, ElSwitch, ElTable, ElTableColumn } from "element-plus";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { getMCPTools, executeMCPTool, updateMCPTool, type MCPTool } from "@/api/mcpTool";

const tools = ref<MCPTool[]>([]);
const loading = ref(false);
const executingTool = ref<MCPTool | null>(null);
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

const handleToggleEnabled = async (row: MCPTool) => {
  ElMessage.info("工具启用/禁用功能需要后端支持");
  loadTools();
};

const handleExecute = async (tool: MCPTool) => {
  executingTool.value = tool;
  executionResult.value = "";
  executing.value = true;

  try {
    const args = parseInput(toolInput.value);
    const response = await executeMCPTool({
      toolName: tool.name,
      arguments: args,
    });

    if (response.success !== false) {
      executionResult.value = typeof response === "string" ? response : JSON.stringify(response, null, 2);
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

const getToolTypeTag = (type: string) => {
  const typeMap: Record<string, string> = {
    builtin: "",
    remote: "warning",
    custom: "info",
  };
  return typeMap[type] || "info";
};

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return "-";
  return dateStr;
};

onMounted(() => {
  loadTools();
});
</script>

<template>
  <div class="mcp-container">
    <div class="mcp-header">
      <span class="mcp-title">MCP 工具管理</span>
      <el-button :icon="useRenderIcon('ep:refresh')" @click="loadTools">刷新</el-button>
    </div>

    <div class="mcp-content">
      <div class="tool-list-section">
        <el-table :data="tools" v-loading="loading" style="width: 100%">
          <el-table-column label="工具" min-width="200">
            <template #default="{ row }">
              <div class="tool-cell">
                <el-icon :size="20" class="tool-icon">
                  <component :is="useRenderIcon(getToolIcon(row.name))" />
                </el-icon>
                <div class="tool-info">
                  <span class="tool-name">{{ row.name }}</span>
                  <span class="tool-desc">{{ row.description }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getToolTypeTag(row.tool_type)" size="small">
                {{ row.tool_type || "builtin" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="调用次数" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.execution_count || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最后执行" width="160" align="center">
            <template #default="{ row }">
              <span>{{ formatDate(row.last_executed_at) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.is_enabled === 1"
                @change="handleToggleEnabled(row)"
                active-text="启用"
                inactive-text="禁用"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="executingTool = row; toolInput = ''; executionResult = ''">
                测试
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="tools.length === 0 && !loading" class="empty-tools">
          <el-icon :size="48" color="var(--el-text-color-placeholder)">
            <component :is="useRenderIcon('ri:tools-line')" />
          </el-icon>
          <span>暂无可用工具</span>
        </div>
      </div>

      <div v-if="executingTool" class="tool-test-section">
        <div class="test-header">
          <el-tag>测试: {{ executingTool.name }}</el-tag>
          <el-button text @click="executingTool = null">关闭</el-button>
        </div>

        <div class="test-body">
          <div class="input-area">
            <p class="input-hint">输入参数 (JSON 格式 或 纯文本)</p>
            <el-input
              v-model="toolInput"
              type="textarea"
              :rows="4"
              placeholder='{"query": "search term"} 或 直接输入搜索词'
            />
          </div>

          <el-button type="primary" :loading="executing" @click="handleExecute(executingTool)">
            执行
          </el-button>

          <div v-if="executionResult" class="result-area">
            <p class="result-label">执行结果:</p>
            <pre class="result-content">{{ executionResult }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.mcp-container {
  padding: 20px;
  height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
}

.mcp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.mcp-title {
  font-size: 18px;
  font-weight: 500;
}

.mcp-content {
  flex: 1;
  display: flex;
  gap: 20px;
  overflow: hidden;
}

.tool-list-section {
  flex: 1;
  overflow: auto;
}

.tool-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tool-icon {
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
}

.tool-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.tool-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.tool-desc {
  font-size: 12px;
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
  padding: 60px 20px;
  gap: 12px;
  color: var(--el-text-color-placeholder);
}

.tool-test-section {
  width: 400px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: auto;
}

.test-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.test-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

.result-area {
  margin-top: 8px;
}

.result-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

.result-content {
  background: var(--el-fill-color);
  border-radius: 6px;
  padding: 12px;
  font-size: 13px;
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}
</style>
