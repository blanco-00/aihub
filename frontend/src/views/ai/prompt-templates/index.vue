<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getPromptTemplateList,
  getPromptTemplate,
  createPromptTemplate,
  updatePromptTemplate,
  deletePromptTemplate,
  renderPromptTemplate,
  getPromptCategories,
  togglePromptTemplateStatus,
  type PromptTemplate,
  type PromptCategory,
  type CreatePromptTemplateRequest,
  type UpdatePromptTemplateRequest,
} from "@/api/promptTemplate";

defineOptions({ name: "PromptTemplates" });

// 列表相关
const loading = ref(false);
const templateList = ref<PromptTemplate[]>([]);
const total = ref(0);
const current = ref(1);
const size = ref(10);
const keyword = ref("");
const categoryId = ref<number | undefined>();

// 分类列表
const categories = ref<PromptCategory[]>([]);

// 对话框相关
const dialogVisible = ref(false);
const dialogTitle = ref("新建模板");
const dialogLoading = ref(false);
const formRef = ref();
const formData = ref({
  id: 0,
  name: "",
  description: "",
  categoryId: undefined as number | undefined,
  content: "",
  variables: {} as Record<string, { label: string; defaultValue?: string }>,
  status: 1,
});

// 变量编辑相关
const variableInput = ref({ key: "", label: "", defaultValue: "" });

// 测试对话框相关
const testDialogVisible = ref(false);
const testTemplate = ref<PromptTemplate | null>(null);
const testVariables = ref<Record<string, string>>({});
const testRenderedContent = ref("");
const testLoading = ref(false);

// 表单校验规则
const formRules = {
  name: [{ required: true, message: "请输入模板名称", trigger: "blur" }],
  categoryId: [{ required: true, message: "请选择分类", trigger: "change" }],
  content: [{ required: true, message: "请输入模板内容", trigger: "blur" }],
};

// 从模板内容中提取变量
const extractVariables = (content: string): string[] => {
  const regex = /\{\{(\w+)\}\}/g;
  const variables: string[] = [];
  let match;
  while ((match = regex.exec(content)) !== null) {
    if (!variables.includes(match[1])) {
      variables.push(match[1]);
    }
  }
  return variables;
};

// 模板内容中的变量列表
const contentVariables = computed(() => {
  return extractVariables(formData.value.content);
});

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getPromptCategories();
    categories.value = res.data || [];
  } catch (error) {
    ElMessage.error("加载分类列表失败");
  }
};

// 加载模板列表
const loadTemplates = async () => {
  loading.value = true;
  try {
    const res = await getPromptTemplateList({
      categoryId: categoryId.value,
      keyword: keyword.value,
      current: current.value,
      size: size.value,
    });
    templateList.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch (error) {
    ElMessage.error("加载模板列表失败");
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  current.value = 1;
  loadTemplates();
};

// 重置搜索
const handleReset = () => {
  keyword.value = "";
  categoryId.value = undefined;
  current.value = 1;
  loadTemplates();
};

// 分页改变
const handlePageChange = (page: number) => {
  current.value = page;
  loadTemplates();
};

// 每页数量改变
const handleSizeChange = (val: number) => {
  size.value = val;
  current.value = 1;
  loadTemplates();
};

// 打开新建对话框
const handleCreate = () => {
  dialogTitle.value = "新建模板";
  formData.value = {
    id: 0,
    name: "",
    description: "",
    categoryId: undefined,
    content: "",
    variables: {},
    status: 1,
  };
  variableInput.value = { key: "", label: "", defaultValue: "" };
  dialogVisible.value = true;
};

// 打开编辑对话框
const handleEdit = async (row: PromptTemplate) => {
  dialogTitle.value = "编辑模板";
  try {
    const res = await getPromptTemplate(row.id);
    if (res.data) {
      formData.value = {
        id: res.data.id,
        name: res.data.name,
        description: res.data.description,
        categoryId: res.data.categoryId,
        content: res.data.content,
        variables: res.data.variables || {},
        status: res.data.status,
      };
      dialogVisible.value = true;
    }
  } catch (error) {
    ElMessage.error("获取模板详情失败");
  }
};

// 添加变量
const handleAddVariable = () => {
  if (!variableInput.value.key.trim()) {
    ElMessage.warning("请输入变量名");
    return;
  }
  if (formData.value.variables[variableInput.value.key]) {
    ElMessage.warning("变量名已存在");
    return;
  }
  formData.value.variables[variableInput.value.key] = {
    label: variableInput.value.label || variableInput.value.key,
    defaultValue: variableInput.value.defaultValue,
  };
  variableInput.value = { key: "", label: "", defaultValue: "" };
};

// 删除变量
const handleRemoveVariable = (key: string) => {
  delete formData.value.variables[key];
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return;
    dialogLoading.value = true;
    try {
      if (formData.value.id) {
        // 更新
        const request: UpdatePromptTemplateRequest = {
          id: formData.value.id,
          name: formData.value.name,
          description: formData.value.description,
          categoryId: formData.value.categoryId!,
          content: formData.value.content,
          variables: formData.value.variables,
          status: formData.value.status,
        };
        await updatePromptTemplate(request);
        ElMessage.success("更新成功");
      } else {
        // 创建
        const request: CreatePromptTemplateRequest = {
          name: formData.value.name,
          description: formData.value.description,
          categoryId: formData.value.categoryId!,
          content: formData.value.content,
          variables: formData.value.variables,
        };
        await createPromptTemplate(request);
        ElMessage.success("创建成功");
      }
      dialogVisible.value = false;
      loadTemplates();
    } catch (error: any) {
      ElMessage.error(error.message || "操作失败");
    } finally {
      dialogLoading.value = false;
    }
  });
};

// 删除模板
const handleDelete = async (row: PromptTemplate) => {
  if (row.isBuiltin === 1) {
    ElMessage.warning("内置模板不可删除");
    return;
  }
  try {
    await ElMessageBox.confirm("确定要删除该模板吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    await deletePromptTemplate(row.id);
    ElMessage.success("删除成功");
    loadTemplates();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "删除失败");
    }
  }
};

// 切换状态
const handleToggleStatus = async (row: PromptTemplate) => {
  const newStatus = row.status === 1 ? 0 : 1;
  try {
    await togglePromptTemplateStatus(row.id, newStatus);
    ElMessage.success("状态更新成功");
    loadTemplates();
  } catch (error: any) {
    ElMessage.error(error.message || "状态更新失败");
  }
};

// 打开测试对话框
const handleTest = async (row: PromptTemplate) => {
  try {
    const res = await getPromptTemplate(row.id);
    if (res.data) {
      testTemplate.value = res.data;
      // 初始化变量值
      testVariables.value = {};
      if (res.data.variables) {
        Object.entries(res.data.variables).forEach(([key, config]) => {
          testVariables.value[key] = config.defaultValue || "";
        });
      }
      // 自动提取变量
      const extractedVars = extractVariables(res.data.content);
      extractedVars.forEach((v) => {
        if (!testVariables.value[v]) {
          testVariables.value[v] = "";
        }
      });
      testRenderedContent.value = "";
      testDialogVisible.value = true;
    }
  } catch (error) {
    ElMessage.error("获取模板详情失败");
  }
};

// 渲染预览
const handleRender = async () => {
  if (!testTemplate.value) return;
  testLoading.value = true;
  try {
    const res = await renderPromptTemplate(
      testTemplate.value.id,
      testVariables.value,
    );
    testRenderedContent.value = res.data || "";
    ElMessage.success("渲染成功");
  } catch (error: any) {
    ElMessage.error(error.message || "渲染失败");
  } finally {
    testLoading.value = false;
  }
};

// 获取分类名称
const getCategoryName = (id: number) => {
  const category = categories.value.find((c) => c.id === id);
  return category?.name || "-";
};

onMounted(() => {
  loadCategories();
  loadTemplates();
});
</script>

<template>
  <div class="prompt-templates">
    <el-card shadow="never">
      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="keyword"
            placeholder="搜索模板名称或描述"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="categoryId"
            placeholder="全部分类"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="primary" @click="handleCreate">新建模板</el-button>
        </el-form-item>
      </el-form>

      <!-- 模板列表 -->
      <el-table v-loading="loading" :data="templateList" stripe>
        <el-table-column prop="name" label="模板名称" width="180" />
        <el-table-column prop="categoryName" label="分类" width="120">
          <template #default="{ row }">
            {{ row.categoryName || getCategoryName(row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="描述"
          show-overflow-tooltip
        />
        <el-table-column prop="isBuiltin" label="类型" width="80">
          <template #default="{ row }">
            <el-tag
              :type="row.isBuiltin === 1 ? 'info' : 'success'"
              size="small"
            >
              {{ row.isBuiltin === 1 ? "内置" : "自定义" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :disabled="row.isBuiltin === 1"
              @change="handleToggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="primary"
              link
              size="small"
              @click="handleTest(row)"
            >
              测试
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              :disabled="row.isBuiltin === 1"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="current"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select
            v-model="formData.categoryId"
            placeholder="请选择分类"
            style="width: 100%"
          >
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模板描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="2"
            placeholder="请输入模板描述"
          />
        </el-form-item>
        <el-form-item label="模板内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="8"
            placeholder="请输入模板内容，支持 {{变量名}} 语法"
          />
          <div v-if="contentVariables.length > 0" class="variable-tip">
            <span>检测到变量：</span>
            <el-tag
              v-for="v in contentVariables"
              :key="v"
              size="small"
              style="margin-right: 4px"
            >
              <span v-html="`{{${v}}}`"></span>
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item label="变量定义">
          <div class="variable-editor">
            <div class="variable-input-row">
              <el-input
                v-model="variableInput.key"
                placeholder="变量名"
                style="width: 120px"
              />
              <el-input
                v-model="variableInput.label"
                placeholder="显示名称"
                style="width: 150px"
              />
              <el-input
                v-model="variableInput.defaultValue"
                placeholder="默认值"
                style="width: 150px"
              />
              <el-button type="primary" @click="handleAddVariable"
                >添加</el-button
              >
            </div>
            <div
              v-if="Object.keys(formData.variables).length > 0"
              class="variable-list"
            >
              <div
                v-for="(config, key) in formData.variables"
                :key="key"
                class="variable-item"
              >
                <el-tag><span v-html="`{{${key}}}`"></span></el-tag>
                <span class="variable-label">{{ config.label }}</span>
                <span v-if="config.defaultValue" class="variable-default">
                  (默认: {{ config.defaultValue }})
                </span>
                <el-button
                  type="danger"
                  link
                  size="small"
                  @click="handleRemoveVariable(key as string)"
                >
                  删除
                </el-button>
              </div>
            </div>
            <el-empty v-else description="暂无变量定义" :image-size="60" />
          </div>
        </el-form-item>
        <el-form-item v-if="formData.id" label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="dialogLoading"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 测试对话框 -->
    <el-dialog
      v-model="testDialogVisible"
      title="模板测试"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="testTemplate" class="test-container">
        <div class="test-info">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="模板名称">
              {{ testTemplate.name }}
            </el-descriptions-item>
            <el-descriptions-item label="分类">
              {{
                testTemplate.categoryName ||
                getCategoryName(testTemplate.categoryId)
              }}
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">
              {{ testTemplate.description }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider content-position="left">变量输入</el-divider>

        <div
          v-if="Object.keys(testVariables).length > 0"
          class="test-variables"
        >
          <el-form label-width="120px">
            <el-form-item
              v-for="(value, key) in testVariables"
              :key="key"
              :label="testTemplate.variables?.[key]?.label || key"
            >
              <el-input
                v-model="testVariables[key]"
                :placeholder="`请输入 ${key}`"
              />
            </el-form-item>
          </el-form>
        </div>
        <el-empty v-else description="该模板没有定义变量" :image-size="60" />

        <el-divider content-position="left">渲染预览</el-divider>

        <div class="test-preview">
          <el-button
            type="primary"
            :loading="testLoading"
            style="margin-bottom: 12px"
            @click="handleRender"
          >
            渲染预览
          </el-button>
          <div class="preview-content">
            <pre>{{
              testRenderedContent || "点击上方按钮进行渲染预览..."
            }}</pre>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.prompt-templates {
  padding: 16px;
}

.search-form {
  margin-bottom: 16px;
}

.variable-tip {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}

.variable-editor {
  width: 100%;
}

.variable-input-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.variable-list {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 8px;
}

.variable-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;

  &:last-child {
    border-bottom: none;
  }
}

.variable-label {
  color: #606266;
}

.variable-default {
  color: #909399;
  font-size: 12px;
}

.test-container {
  .test-info {
    margin-bottom: 16px;
  }

  .test-variables {
    margin-bottom: 16px;
  }

  .test-preview {
    .preview-content {
      background: #f5f7fa;
      border-radius: 4px;
      padding: 16px;
      min-height: 150px;

      pre {
        margin: 0;
        white-space: pre-wrap;
        word-break: break-word;
        font-family: inherit;
        color: #303133;
      }
    }
  }
}
</style>
