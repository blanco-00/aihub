<template>
  <el-form
    ref="formRef"
    :model="newFormInline"
    :rules="rules"
    label-width="100px"
  >
    <el-form-item label="分类名称" prop="name">
      <el-input
        v-model="newFormInline.name"
        placeholder="请输入分类名称"
        clearable
      />
    </el-form-item>
    <el-form-item label="分类代码" prop="code">
      <el-input
        v-model="newFormInline.code"
        placeholder="请输入分类代码（唯一）"
        clearable
      />
    </el-form-item>
    <el-form-item label="分类描述" prop="description">
      <el-input
        v-model="newFormInline.description"
        type="textarea"
        :rows="3"
        placeholder="请输入分类描述"
        clearable
      />
    </el-form-item>
    <el-form-item label="排序" prop="sortOrder">
      <el-input-number
        v-model="newFormInline.sortOrder"
        :min="0"
        placeholder="排序（数字越大越靠前）"
        style="width: 100%"
      />
    </el-form-item>
    <el-form-item label="状态" prop="status">
      <el-radio-group v-model="newFormInline.status">
        <el-radio :label="1">启用</el-radio>
        <el-radio :label="0">禁用</el-radio>
      </el-radio-group>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import type { FormInstance } from "element-plus";
import { formRules } from "./rule";

defineOptions({
  name: "NoticeCategoryForm",
});

interface FormProps {
  formInline: {
    id?: number;
    name: string;
    code: string;
    description: string;
    sortOrder: number;
    status: number;
  };
}

const props = withDefaults(defineProps<FormProps>(), {
  formInline: () => ({
    id: undefined,
    name: "",
    code: "",
    description: "",
    sortOrder: 0,
    status: 1,
  }),
});

const formRef = ref<FormInstance>();
const newFormInline = ref(props.formInline);

const rules = formRules;

// 监听 formInline 变化，同步到 newFormInline
watch(
  () => props.formInline,
  (newVal) => {
    newFormInline.value = { ...newVal };
  },
  { deep: true, immediate: true },
);

function getRef() {
  return formRef.value;
}

function getFormData() {
  return newFormInline.value;
}

defineExpose({ getRef, getFormData });
</script>
