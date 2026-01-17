<template>
  <el-form
    ref="formRef"
    :model="newFormInline"
    :rules="rules"
    label-width="100px"
  >
    <el-form-item label="通知标题" prop="title">
      <el-input
        v-model="newFormInline.title"
        placeholder="请输入通知标题"
        clearable
      />
    </el-form-item>
    <el-form-item label="通知分类" prop="categoryId">
      <el-select
        v-model="newFormInline.categoryId"
        placeholder="请选择分类"
        clearable
        style="width: 100%"
      >
        <el-option
          v-for="item in categoryOptions"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="通知类型" prop="type">
      <el-radio-group v-model="newFormInline.type">
        <el-radio :label="1">普通通知</el-radio>
        <el-radio :label="2">重要通知</el-radio>
        <el-radio :label="3">紧急通知</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="发布范围" prop="publishType">
      <el-radio-group v-model="newFormInline.publishType" @change="handlePublishTypeChange">
        <el-radio :label="1">全部用户</el-radio>
        <el-radio :label="2">指定部门</el-radio>
        <el-radio :label="3">指定角色</el-radio>
        <el-radio :label="4">指定用户</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      v-if="newFormInline.publishType === 2"
      label="选择部门"
      prop="departmentIds"
    >
      <el-select
        v-model="newFormInline.departmentIds"
        multiple
        placeholder="请选择部门"
        style="width: 100%"
        filterable
      >
        <el-option
          v-for="item in departmentOptions"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
    </el-form-item>
    <el-form-item
      v-if="newFormInline.publishType === 3"
      label="选择角色"
      prop="roleIds"
    >
      <el-select
        v-model="newFormInline.roleIds"
        multiple
        placeholder="请选择角色"
        style="width: 100%"
        filterable
      >
        <el-option
          v-for="item in roleOptions"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
    </el-form-item>
    <el-form-item
      v-if="newFormInline.publishType === 4"
      label="选择用户"
      prop="userIds"
    >
      <el-select
        v-model="newFormInline.userIds"
        multiple
        placeholder="请选择用户"
        style="width: 100%"
        filterable
      >
        <el-option
          v-for="item in userOptions"
          :key="item.id"
          :label="item.nickname || item.username"
          :value="item.id"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="通知内容" prop="content">
      <el-input
        v-model="newFormInline.content"
        type="textarea"
        :rows="8"
        placeholder="请输入通知内容（支持HTML）"
        clearable
        show-word-limit
        :maxlength="5000"
        resize="vertical"
      />
    </el-form-item>
    <el-form-item label="过期时间" prop="expireTime">
      <el-date-picker
        v-model="newFormInline.expireTime"
        type="datetime"
        placeholder="选择过期时间（可选）"
        style="width: 100%"
        format="YYYY-MM-DD HH:mm:ss"
        value-format="YYYY-MM-DD HH:mm:ss"
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
  </el-form>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import type { FormInstance } from "element-plus";
import { formRules } from "./rule";
import { getNoticeCategoryList } from "@/api/notice";
import { getDepartmentTree } from "@/api/department";
import { getAllRoles } from "@/api/role";
import { getUserList } from "@/api/system";

defineOptions({
  name: "NoticeForm"
});

interface FormProps {
  formInline: {
    id?: number;
    title: string;
    content: string;
    categoryId: number | null;
    type: number;
    publishType: number;
    departmentIds: number[];
    roleIds: number[];
    userIds: number[];
    expireTime: string | null;
    sortOrder: number;
  };
}

const props = withDefaults(defineProps<FormProps>(), {
  formInline: () => ({
    id: undefined,
    title: "",
    content: "",
    categoryId: null,
    type: 1,
    publishType: 1,
    departmentIds: [],
    roleIds: [],
    userIds: [],
    expireTime: null,
    sortOrder: 0
  })
});

const formRef = ref<FormInstance>();
const newFormInline = ref({ ...props.formInline });

const categoryOptions = ref([]);
const departmentOptions = ref([]);
const roleOptions = ref([]);
const userOptions = ref([]);

const rules = formRules;

// 监听 formInline 变化，同步到 newFormInline
watch(() => props.formInline, (newVal) => {
  if (newVal) {
    console.log("表单数据更新:", newVal);
    console.log("通知内容:", newVal.content);
    newFormInline.value = {
      id: newVal.id,
      title: newVal.title || "",
      content: newVal.content ?? "", // 使用 ?? 而不是 ||，避免 content 为 0 时被转换为 ""
      categoryId: newVal.categoryId || null,
      type: newVal.type ?? 1,
      publishType: newVal.publishType ?? 1,
      departmentIds: newVal.departmentIds || [],
      roleIds: newVal.roleIds || [],
      userIds: newVal.userIds || [],
      expireTime: newVal.expireTime || null,
      sortOrder: newVal.sortOrder ?? 0
    };
    console.log("更新后的表单数据:", newFormInline.value);
    console.log("更新后的通知内容:", newFormInline.value.content);
  }
}, { deep: true, immediate: true });

// 加载分类选项
function loadCategoryOptions() {
  getNoticeCategoryList({ current: 1, size: 1000, status: 1 })
    .then((response: any) => {
      // 处理响应格式：可能是 { data: { code: 200, data: {...} } } 或 { code: 200, data: {...} }
      const responseData = response?.data || response;
      if (responseData?.code === 200 && responseData?.data) {
        categoryOptions.value = responseData.data.records || [];
      } else if (responseData?.records) {
        // 如果直接返回分页数据
        categoryOptions.value = responseData.records || [];
      } else {
        categoryOptions.value = [];
      }
      console.log("加载通知分类选项:", categoryOptions.value);
    })
    .catch((error: any) => {
      console.error("加载通知分类选项失败:", error);
      categoryOptions.value = [];
    });
}

// 加载部门选项
function loadDepartmentOptions() {
  getDepartmentTree()
    .then((response: any) => {
      if (response?.data?.code === 200 && response?.data?.data) {
        // 将树形结构扁平化
        const flatten = (items: any[]): any[] => {
          const result: any[] = [];
          items.forEach(item => {
            result.push({ id: item.id, name: item.name });
            if (item.children && item.children.length > 0) {
              result.push(...flatten(item.children));
            }
          });
          return result;
        };
        departmentOptions.value = flatten(response.data.data);
      }
    })
    .catch(() => {
      departmentOptions.value = [];
    });
}

// 加载角色选项
function loadRoleOptions() {
  getAllRoles()
    .then((response: any) => {
      if (response?.data?.code === 200 && response?.data?.data) {
        roleOptions.value = response.data.data || [];
      }
    })
    .catch(() => {
      roleOptions.value = [];
    });
}

// 加载用户选项
function loadUserOptions() {
  getUserList({ currentPage: 1, pageSize: 1000, status: 1 })
    .then((response: any) => {
      if (response?.data?.code === 0 && response?.data?.data?.list) {
        userOptions.value = response.data.data.list || [];
      }
    })
    .catch(() => {
      userOptions.value = [];
    });
}

function handlePublishTypeChange() {
  // 切换发布范围时，清空之前的选择
  newFormInline.value.departmentIds = [];
  newFormInline.value.roleIds = [];
  newFormInline.value.userIds = [];
}

onMounted(() => {
  loadCategoryOptions();
  loadDepartmentOptions();
  loadRoleOptions();
  loadUserOptions();
});

function getRef() {
  return formRef.value;
}

function getFormData() {
  return newFormInline.value;
}

defineExpose({ getRef, getFormData });
</script>
