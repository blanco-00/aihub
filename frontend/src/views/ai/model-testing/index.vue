<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import {
  getEnabledModelConfigs,
  chatWithModel,
  checkModelHealth,
  compareModels,
  getTestHistory,
  type ModelConfig,
  type ModelTestResult,
  type MultiModelComparisonRequest,
  type MultiModelComparisonResponse,
} from "@/api/modelConfig";

defineOptions({ name: "ModelTesting" });

const activeTab = ref("single");
const models = ref<ModelConfig[]>([]);
const selectedModel = ref<ModelConfig | null>(null);
const selectedModels = ref<ModelConfig[]>([]);
const message = ref("");
const response = ref("");
const loading = ref(false);
const modelLoading = ref(false);
const compareLoading = ref(false);

const history = ref<ModelTestResult[]>([]);
const historyLoading = ref(false);
const historyTotal = ref(0);
const historyCurrent = ref(1);
const historySize = ref(10);
const historyKeyword = ref("");

const loadModels = async () => {
  try {
    const res = await getEnabledModelConfigs();
    models.value = res.data || [];
    if (models.value.length > 0 && !selectedModel.value) {
      selectedModel.value = models.value[0];
    }
  } catch (error) {
    ElMessage.error("加载模型列表失败");
  }
};

const testHealth = async () => {
  if (!selectedModel.value) return;
  modelLoading.value = true;
  try {
    const res = await checkModelHealth(selectedModel.value.id);
    if (res.data) {
      ElMessage.success("模型可用");
    } else {
      ElMessage.warning("模型不可用");
    }
  } catch (error) {
    ElMessage.error("健康检查失败");
  } finally {
    modelLoading.value = false;
  }
};

const sendMessage = async () => {
  if (!selectedModel.value) {
    ElMessage.warning("请选择模型");
    return;
  }
  if (!message.value.trim()) {
    ElMessage.warning("请输入消息");
    return;
  }

  loading.value = true;
  response.value = "";
  try {
    const res = await chatWithModel(selectedModel.value.id, message.value);
    response.value = res.data || "无响应";
    ElMessage.success("发送成功");
  } catch (error: any) {
    response.value = `错误: ${error.message || "未知错误"}`;
    ElMessage.error("发送失败");
  } finally {
    loading.value = false;
  }
};

const compareModelsTest = async () => {
  if (selectedModels.value.length === 0) {
    ElMessage.warning("请至少选择一个模型");
    return;
  }
  if (!message.value.trim()) {
    ElMessage.warning("请输入测试消息");
    return;
  }

  compareLoading.value = true;
  try {
    const request: MultiModelComparisonRequest = {
      modelIds: selectedModels.value.map((m) => m.id),
      message: message.value,
    };
    const res = await compareModels(request);
    const data = res.data as MultiModelComparisonResponse;
    history.value = data.results || [];
    historyTotal.value = data.totalModels || 0;
    ElMessage.success(
      `对比完成：成功 ${data.successCount} 个，失败 ${data.failureCount} 个`,
    );
  } catch (error: any) {
    ElMessage.error("对比失败: " + (error.message || "未知错误"));
  } finally {
    compareLoading.value = false;
  }
};

const loadTestHistory = async () => {
  historyLoading.value = true;
  try {
    const res = await getTestHistory({
      modelId: selectedModel.value?.id,
      keyword: historyKeyword.value,
      current: historyCurrent.value,
      size: historySize.value,
    });
    history.value = res.data.records || [];
    historyTotal.value = res.data.total || 0;
  } catch (error) {
    ElMessage.error("加载测试历史失败");
  } finally {
    historyLoading.value = false;
  }
};

const handlePageChange = (page: number) => {
  historyCurrent.value = page;
  loadTestHistory();
};

const handleSearch = () => {
  historyCurrent.value = 1;
  loadTestHistory();
};

onMounted(() => {
  loadModels();
  loadTestHistory();
});
</script>

<template>
  <div class="model-testing">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="单模型测试" name="single">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="never">
              <template #header>
                <span>选择模型</span>
              </template>
              <el-select
                v-model="selectedModel"
                placeholder="请选择模型"
                value-key="id"
                style="width: 100%"
              >
                <el-option
                  v-for="model in models"
                  :key="model.id"
                  :label="model.name"
                  :value="model"
                />
              </el-select>
              <el-button
                type="primary"
                :loading="modelLoading"
                style="width: 100%; margin-top: 12px"
                @click="testHealth"
              >
                健康检查
              </el-button>
              <div v-if="selectedModel" style="margin-top: 12px">
                <el-descriptions :column="1" border size="small">
                  <el-descriptions-item label="厂商">
                    {{ selectedModel.vendor }}
                  </el-descriptions-item>
                  <el-descriptions-item label="模型">
                    {{ selectedModel.modelId }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-card>
          </el-col>
          <el-col :span="18">
            <el-card shadow="never" style="min-height: 500px">
              <template #header>
                <span>对话测试</span>
              </template>
              <el-input
                v-model="message"
                type="textarea"
                :rows="4"
                placeholder="请输入测试消息..."
                style="margin-bottom: 12px"
              />
              <el-button type="primary" :loading="loading" @click="sendMessage">
                发送
              </el-button>
              <el-divider />
              <div class="response-area">
                <div class="response-label">模型回复：</div>
                <div class="response-content">
                  {{ response || "等待回复..." }}
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="多模型对比" name="compare">
        <el-card shadow="never">
          <template #header>
            <span>模型对比测试</span>
          </template>
          <el-form label-width="100px">
            <el-form-item label="选择模型">
              <el-select
                v-model="selectedModels"
                placeholder="请选择要对比的模型（可多选）"
                multiple
                value-key="id"
                style="width: 100%"
              >
                <el-option
                  v-for="model in models"
                  :key="model.id"
                  :label="model.name"
                  :value="model"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="测试消息">
              <el-input
                v-model="message"
                type="textarea"
                :rows="4"
                placeholder="请输入测试消息..."
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="compareLoading"
                @click="compareModelsTest"
              >
                开始对比
              </el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <div v-if="history.length > 0" class="comparison-results">
            <h3>对比结果</h3>
            <el-row :gutter="20">
              <el-col v-for="result in history" :key="result.modelId" :span="8">
                <el-card shadow="hover" class="result-card">
                  <template #header>
                    <div class="result-header">
                      <span>{{ result.modelName }}</span>
                      <el-tag
                        :type="result.success ? 'success' : 'danger'"
                        size="small"
                      >
                        {{ result.success ? "成功" : "失败" }}
                      </el-tag>
                    </div>
                  </template>
                  <div class="result-content">
                    <el-descriptions :column="1" size="small">
                      <el-descriptions-item label="厂商">
                        {{ result.vendor }}
                      </el-descriptions-item>
                      <el-descriptions-item label="响应时间">
                        {{ result.responseTimeMs }}ms
                      </el-descriptions-item>
                    </el-descriptions>
                    <div class="result-response">
                      <div class="result-label">回复内容：</div>
                      <div class="result-text">
                        {{ result.response || result.error || "无响应" }}
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
          <el-empty v-else description="请选择模型并输入消息进行对比" />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="测试历史" name="history">
        <el-card shadow="never">
          <template #header>
            <span>测试历史记录</span>
          </template>

          <el-form :inline="true" style="margin-bottom: 16px">
            <el-form-item label="模型">
              <el-select
                v-model="selectedModel"
                placeholder="选择模型"
                clearable
                value-key="id"
                style="width: 200px"
              >
                <el-option
                  v-for="model in models"
                  :key="model.id"
                  :label="model.name"
                  :value="model"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="关键词">
              <el-input
                v-model="historyKeyword"
                placeholder="搜索消息或回复"
                style="width: 200px"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="history" :loading="historyLoading" stripe>
            <el-table-column prop="modelName" label="模型" width="150" />
            <el-table-column prop="vendor" label="厂商" width="100" />
            <el-table-column
              prop="userMessage"
              label="用户消息"
              show-overflow-tooltip
            />
            <el-table-column
              prop="response"
              label="模型回复"
              show-overflow-tooltip
            />
            <el-table-column prop="success" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.success ? 'success' : 'danger'" size="small">
                  {{ row.success ? "成功" : "失败" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="responseTimeMs" label="响应时间" width="100">
              <template #default="{ row }">
                {{ row.responseTimeMs }}ms
              </template>
            </el-table-column>
            <el-table-column prop="timestamp" label="时间" width="180" />
          </el-table>

          <el-pagination
            v-model:current-page="historyCurrent"
            v-model:page-size="historySize"
            :total="historyTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            style="margin-top: 16px; justify-content: flex-end"
            @current-change="handlePageChange"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style lang="scss" scoped>
.model-testing {
  padding: 16px;
}

.response-area {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  min-height: 200px;
}

.response-label {
  font-weight: bold;
  margin-bottom: 8px;
  color: #606266;
}

.response-content {
  white-space: pre-wrap;
  word-break: break-word;
  color: #303133;
}

.comparison-results {
  margin-top: 20px;
}

.comparison-results h3 {
  margin-bottom: 16px;
  color: #303133;
}

.result-card {
  margin-bottom: 20px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-response {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.result-label {
  font-weight: bold;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.result-text {
  white-space: pre-wrap;
  word-break: break-word;
  color: #303133;
  font-size: 13px;
  max-height: 150px;
  overflow-y: auto;
}
</style>
