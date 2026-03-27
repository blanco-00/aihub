<script setup lang="ts">
defineOptions({
  name: "SystemModelConfig",
});

import { ref, reactive, onMounted, h } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { TableColumnData } from "@pureadmin/table";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { PureTableBar } from "@/components/RePureTableBar";
import {
  getModelConfigList,
  deleteModelConfig,
  toggleModelConfigStatus,
  type ModelConfig,
  type ModelConfigListParams,
} from "@/api/modelConfig";
import ModelConfigDialog from "./components/ModelConfigDialog.vue";

import Delete from "~icons/ep/delete";
import EditPen from "~icons/ep/edit-pen";
import Refresh from "~icons/ep/refresh";
import AddFill from "~icons/ri/add-circle-line";

const tableRef = ref();
const dialogRef = ref();
const loading = ref(false);
const dataList = ref<ModelConfig[]>([]);
const pagination = reactive({
  total: 0,
  currentPage: 1,
  pageSize: 10,
});

const searchForm = reactive<ModelConfigListParams>({
  keyword: "",
  vendor: "",
  status: undefined,
  modelType: undefined,
  current: 1,
  size: 10,
});

const vendorOptions = [
  { label: "OpenAI", value: "openai" },
  { label: "Anthropic", value: "anthropic" },
  { label: "Azure", value: "azure" },
  { label: "百度", value: "baidu" },
  { label: "阿里", value: "ali" },
  { label: "腾讯", value: "tencent" },
  { label: "智谱", value: "zhipuai" },
];

const modelTypeOptions = [
  { label: "对话模型", value: "chat" },
  { label: "向量模型", value: "embedding" },
  { label: "文生图模型", value: "image" },
  { label: "语音模型", value: "audio" },
  { label: "重排序模型", value: "rerank" },
];

const modelTypeMap: Record<string, string> = {
  chat: "对话",
  embedding: "向量",
  image: "文生图",
  audio: "语音",
  rerank: "重排序",
};

const vendorMap: Record<string, string> = {
  openai: "OpenAI",
  anthropic: "Anthropic",
  azure: "Azure",
  baidu: "百度",
  ali: "阿里",
  tencent: "腾讯",
  zhipuai: "智谱",
};

const columns: TableColumnData[] = [
  {
    label: "模型名称",
    prop: "name",
    minWidth: 120,
  },
  {
    label: "厂商",
    prop: "vendor",
    minWidth: 100,
    cellRenderer: ({ row }: { row: ModelConfig }) => {
      return h("span", vendorMap[row.vendor] || row.vendor);
    },
  },
  {
    label: "模型ID",
    prop: "modelId",
    minWidth: 180,
  },
  {
    label: "Base URL",
    prop: "baseUrl",
    minWidth: 200,
    showOverflowTooltip: true,
  },
  {
    label: "状态",
    prop: "status",
    minWidth: 100,
    cellRenderer: ({ row }: { row: ModelConfig }) => {
      return row.status === 1
        ? h("el-tag", { type: "success" }, "已启用")
        : h("el-tag", { type: "info" }, "已禁用");
    },
  },
  {
    label: "类型",
    prop: "modelType",
    minWidth: 100,
    cellRenderer: ({ row }: { row: ModelConfig }) => {
      return h(
        "el-tag",
        { type: row.modelType === "chat" ? "" : "warning" },
        modelTypeMap[row.modelType || "chat"] || row.modelType || "对话"
      );
    },
  },
  {
    label: "创建时间",
    prop: "createdAt",
    minWidth: 180,
  },
  {
    label: "操作",
    fixed: "right",
    width: 200,
    slot: "operation",
  },
];

const fetchData = async () => {
  loading.value = true;
  try {
    const response = await getModelConfigList(searchForm);
    if (response.code === 200) {
      dataList.value = response.data.records || [];
      pagination.total = response.data.total || 0;
    }
  } catch (error: any) {
    console.error("获取模型列表失败", error);
    ElMessage.error("获取模型列表失败: " + error.message);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  searchForm.current = 1;
  fetchData();
};

const handleReset = () => {
  searchForm.keyword = "";
  searchForm.vendor = "";
  searchForm.status = undefined;
  searchForm.modelType = undefined;
  searchForm.current = 1;
  fetchData();
};

const handleSizeChange = (size: number) => {
  searchForm.size = size;
  searchForm.current = 1;
  fetchData();
};

const handleCurrentChange = (current: number) => {
  searchForm.current = current;
  fetchData();
};

const handleOpenDialog = (mode: "create" | "edit", row?: ModelConfig) => {
  dialogRef.value?.openDialog(mode, row);
};

const handleDelete = (row: ModelConfig) => {
  ElMessageBox.confirm(
    `确认删除模型配置 "${row.name}" 吗？此操作不可恢复。`,
    "删除确认",
    {
      type: "warning",
      confirmButtonText: "确认",
      cancelButtonText: "取消",
    },
  )
    .then(async () => {
      try {
        const response = await deleteModelConfig(row.id);
        if (response.code === 200) {
          ElMessage.success("删除成功");
          fetchData();
        }
      } catch (error: any) {
        console.error("删除模型配置失败", error);
        ElMessage.error("删除模型配置失败: " + error.message);
      }
    })
    .catch(() => {});
};

const handleToggleStatus = async (row: ModelConfig) => {
  const newStatus = row.status === 1 ? 0 : 1;
  const statusText = newStatus === 1 ? "启用" : "禁用";
  try {
    const response = await toggleModelConfigStatus(row.id, newStatus);
    if (response.code === 200) {
      ElMessage.success(`${statusText}成功`);
      fetchData();
    }
  } catch (error: any) {
    console.error(`${statusText}模型配置失败`, error);
    ElMessage.error(`${statusText}模型配置失败: ` + error.message);
  }
};

const handleDialogSuccess = () => {
  fetchData();
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="model-management-container">
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="模型名称：" prop="keyword">
        <el-input
          v-model="searchForm.keyword"
          placeholder="请输入模型名称"
          clearable
          class="w-[180px]!"
        />
      </el-form-item>
      <el-form-item label="厂商：" prop="vendor">
        <el-select
          v-model="searchForm.vendor"
          placeholder="请选择厂商"
          clearable
          class="w-[180px]!"
        >
          <el-option
            v-for="option in vendorOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态：" prop="status">
        <el-select
          v-model="searchForm.status"
          placeholder="请选择状态"
          clearable
          class="w-[180px]!"
        >
          <el-option label="已启用" :value="1" />
          <el-option label="已禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型：" prop="modelType">
        <el-select
          v-model="searchForm.modelType"
          placeholder="请选择类型"
          clearable
          class="w-[180px]!"
        >
          <el-option
            v-for="option in modelTypeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          :icon="useRenderIcon('ri/search-line')"
          :loading="loading"
          @click="handleSearch"
        >
          搜索
        </el-button>
        <el-button :icon="useRenderIcon(Refresh)" @click="handleReset">
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <PureTableBar title="模型管理" :columns="columns" @refresh="fetchData">
      <template #buttons>
        <el-button
          type="primary"
          :icon="useRenderIcon(AddFill)"
          @click="handleOpenDialog('create')"
        >
          新增模型
        </el-button>
      </template>
      <template v-slot="{ size, dynamicColumns }">
        <pure-table
          ref="tableRef"
          row-key="id"
          adaptive
          :adaptiveConfig="{ offsetBottom: 108 }"
          align-whole="center"
          table-layout="auto"
          :loading="loading"
          :size="size"
          :data="dataList"
          :columns="dynamicColumns"
          :pagination="{ ...pagination, size }"
          :header-cell-style="{
            background: 'var(--el-fill-color-light)',
            color: 'var(--el-text-color-primary)',
          }"
          @page-size-change="handleSizeChange"
          @page-current-change="handleCurrentChange"
        >
          <template #operation="{ row }">
            <el-button
              class="reset-margin"
              link
              type="primary"
              :size="size"
              :icon="useRenderIcon(EditPen)"
              @click="handleOpenDialog('edit', row)"
            >
              编辑
            </el-button>
            <el-button
              class="reset-margin"
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              :size="size"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? "禁用" : "启用" }}
            </el-button>
            <el-popconfirm
              :title="`确认删除模型配置 '${row.name}' 吗？`"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button
                  class="reset-margin"
                  link
                  type="danger"
                  :size="size"
                  :icon="useRenderIcon(Delete)"
                >
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </pure-table>
      </template>
    </PureTableBar>

    <ModelConfigDialog ref="dialogRef" @success="handleDialogSuccess" />
  </div>
</template>

<style lang="scss" scoped>
.model-management-container {
  height: calc(100vh - 84px);
  padding: 12px;
}

.search-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }

  :deep(.el-form-item__label) {
    color: var(--el-text-color-primary);
  }
}

:deep(.el-button:focus-visible) {
  outline: none;
}
</style>
