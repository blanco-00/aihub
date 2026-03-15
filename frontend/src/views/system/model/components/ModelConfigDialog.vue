<script setup lang="ts">
defineOptions({
  name: "ModelConfigDialog",
});

import { ref, reactive, watch } from "vue";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import {
  createModelConfig,
  updateModelConfig,
  getModelList,
  type ModelConfig,
  type CreateModelConfigRequest,
  type UpdateModelConfigRequest,
} from "@/api/modelConfig";

const emit = defineEmits<{
  success: [];
}>();

const dialogVisible = ref(false);
const dialogTitle = ref("");
const formRef = ref<FormInstance>();
const loading = ref(false);
const submitLoading = ref(false);
const modelLoading = ref(false);

const formData = reactive<CreateModelConfigRequest & { id?: number }>({
  name: "",
  vendor: "",
  modelId: "",
  apiKey: "",
  baseUrl: "",
  status: 1,
  config: "",
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

const modelOptions = ref<string[]>([]);

const formRules: FormRules = {
  name: [
    { required: true, message: "请输入模型名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" },
  ],
  vendor: [{ required: true, message: "请选择厂商", trigger: "change" }],
  modelId: [
    { required: true, message: "请输入或选择模型ID", trigger: "blur" },
    { min: 1, max: 100, message: "长度在 1 到 100 个字符", trigger: "blur" },
  ],
  apiKey: [
    { required: true, message: "请输入API Key", trigger: "blur" },
    { min: 1, max: 500, message: "长度在 1 到 500 个字符", trigger: "blur" },
  ],
  baseUrl: [
    {
      pattern: /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/,
      message: "请输入有效的URL",
      trigger: "blur",
    },
  ],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
};

const fetchModels = async () => {
  if (!formData.vendor || !formData.apiKey) {
    ElMessage.warning("请先选择厂商并输入API Key");
    return;
  }

  modelLoading.value = true;
  try {
    const res = await getModelList(
      formData.vendor,
      formData.apiKey,
      formData.baseUrl || undefined
    );
    if (res.code === 200 && res.data) {
      modelOptions.value = res.data;
      if (res.data.length > 0) {
        ElMessage.success(`成功获取 ${res.data.length} 个模型`);
      } else {
        ElMessage.warning("未获取到模型，请手动输入");
      }
    } else {
      ElMessage.error(res.message || "获取模型列表失败");
    }
  } catch (error: any) {
    console.error("获取模型列表失败", error);
    const errMsg = error?.response?.data?.message || error?.message || "获取模型列表失败";
    ElMessage.error(errMsg);
  } finally {
    modelLoading.value = false;
  }
};

const openDialog = async (mode: "create" | "edit", row?: ModelConfig) => {
  dialogVisible.value = true;
  dialogTitle.value = mode === "create" ? "新增模型" : "编辑模型";
  modelOptions.value = [];

  if (mode === "edit" && row) {
    formData.id = row.id;
    formData.name = row.name;
    formData.vendor = row.vendor;
    formData.modelId = row.modelId;
    formData.apiKey = row.apiKey;
    formData.baseUrl = row.baseUrl || "";
    formData.status = row.status;
    formData.config = row.config || "";
  } else {
    resetForm();
  }
};

const closeDialog = () => {
  dialogVisible.value = false;
  resetForm();
};

const resetForm = () => {
  formData.id = undefined;
  formData.name = "";
  formData.vendor = "";
  formData.modelId = "";
  formData.apiKey = "";
  formData.baseUrl = "";
  formData.status = 1;
  formData.config = "";
  formRef.value?.clearValidate();
};

const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        const request: CreateModelConfigRequest | UpdateModelConfigRequest = {
          name: formData.name,
          vendor: formData.vendor,
          modelId: formData.modelId,
          apiKey: formData.apiKey,
          baseUrl: formData.baseUrl || undefined,
          status: formData.status,
          config: formData.config || undefined,
        };

        let response;
        if (formData.id) {
          response = await updateModelConfig(formData.id, request);
        } else {
          response = await createModelConfig(request);
        }

        if (response.code === 200) {
          ElMessage.success(formData.id ? "更新成功" : "创建成功");
          emit("success");
          closeDialog();
        }
      } catch (error: any) {
        console.error("保存模型配置失败", error);
        ElMessage.error("保存模型配置失败: " + error.message);
      } finally {
        submitLoading.value = false;
      }
    }
  });
};

const handleCancel = () => {
  closeDialog();
};

defineExpose({
  openDialog,
});
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="700px"
    :close-on-click-modal="false"
    @close="handleCancel"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      label-position="right"
    >
      <el-form-item label="模型名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入模型名称，如：GPT-4"
          clearable
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="厂商" prop="vendor">
        <el-select
          v-model="formData.vendor"
          placeholder="请选择厂商"
          class="w-full!"
          @change="modelOptions = []"
        >
          <el-option
            v-for="option in vendorOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="API Key" prop="apiKey">
        <el-input
          v-model="formData.apiKey"
          type="textarea"
          :rows="3"
          placeholder="请输入API Key"
          show-password
          maxlength="500"
          @input="modelOptions = []"
        />
      </el-form-item>

      <el-form-item label="Base URL" prop="baseUrl">
        <div class="w-full">
          <el-input
            v-model="formData.baseUrl"
            placeholder="可选，不填则使用厂商默认地址"
            clearable
          >
            <template #append>
              <el-tooltip content="智谱默认: https://open.bigmodel.cn/api/paas/v4">
                <el-button>?</el-button>
              </el-tooltip>
            </template>
          </el-input>
        </div>
      </el-form-item>

      <el-form-item label="模型ID" prop="modelId">
        <div class="flex gap-2 w-full">
          <el-select
            v-model="formData.modelId"
            placeholder="请选择或输入模型ID"
            filterable
            allow-create
            clearable
            class="flex-1!"
            :disabled="!formData.vendor || !formData.apiKey"
          >
            <el-option
              v-for="model in modelOptions"
              :key="model"
              :label="model"
              :value="model"
            />
            <el-option
              v-if="modelOptions.length === 0 && formData.vendor && formData.apiKey"
              label="点击获取模型或手动输入"
              value=""
              disabled
            />
          </el-select>
          <el-button
            :loading="modelLoading"
            :disabled="!formData.vendor || !formData.apiKey"
            type="primary"
            @click="fetchModels"
          >
            获取模型
          </el-button>
        </div>
        <div class="text-xs text-gray-500 mt-1" v-if="!formData.apiKey">
          请先填写API Key后点击"获取模型"
        </div>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="配置" prop="config">
        <el-input
          v-model="formData.config"
          type="textarea"
          :rows="6"
          placeholder="请输入JSON格式的配置信息（可选）"
          clearable
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
:deep(.el-button:focus-visible) {
  outline: none;
}
</style>
