<script setup lang="ts">
import { ref, onMounted } from "vue";
import ReCol from "@/components/ReCol";
import { formRules } from "../utils/rule";
import { FormProps } from "../utils/types";
import { usePublicHooks } from "../../hooks";
import { getDepartmentTree } from "@/api/department";
import type { DepartmentInfo } from "@/api/department";

const props = withDefaults(defineProps<FormProps>(), {
  formInline: () => ({
    title: "新增",
    nickname: "",
    username: "",
    password: "",
    phone: "",
    email: "",
    sex: "",
    role: "USER",
    departmentId: 0,
    status: 1,
    remark: ""
  })
});

const sexOptions = [
  {
    value: 0,
    label: "男"
  },
  {
    value: 1,
    label: "女"
  }
];

const roleOptions = [
  {
    value: "SUPER_ADMIN",
    label: "超级管理员"
  },
  {
    value: "ADMIN",
    label: "管理员"
  },
  {
    value: "USER",
    label: "普通用户"
  }
];
const ruleFormRef = ref();
const { switchStyle } = usePublicHooks();
const newFormInline = ref(props.formInline);
const departmentTree = ref<DepartmentInfo[]>([]);
const departmentProps = {
  value: "id",
  label: "name",
  children: "children"
};

// 加载部门树
onMounted(async () => {
  try {
    const { code, data } = await getDepartmentTree();
    if (code === 200 && data) {
      // 后端已经返回树结构，直接使用，不需要再用 handleTree 处理
      departmentTree.value = data as DepartmentInfo[];
    }
  } catch (error) {
    console.error("加载部门树失败", error);
  }
});

function getRef() {
  return ruleFormRef.value;
}

defineExpose({ getRef });
</script>

<template>
  <el-form
    ref="ruleFormRef"
    :model="newFormInline"
    :rules="formRules"
    label-width="82px"
  >
    <el-row :gutter="30">
      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="用户昵称" prop="nickname">
          <el-input
            v-model="newFormInline.nickname"
            clearable
            placeholder="请输入用户昵称"
          />
        </el-form-item>
      </re-col>
      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="用户名称" prop="username">
          <el-input
            v-model="newFormInline.username"
            clearable
            placeholder="请输入用户名称"
          />
        </el-form-item>
      </re-col>

      <re-col
        v-if="newFormInline.title === '新增'"
        :value="12"
        :xs="24"
        :sm="24"
      >
        <el-form-item label="用户密码" prop="password">
          <el-input
            v-model="newFormInline.password"
            clearable
            placeholder="请输入用户密码"
          />
        </el-form-item>
      </re-col>
      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="newFormInline.phone"
            clearable
            placeholder="请输入手机号"
          />
        </el-form-item>
      </re-col>

      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="newFormInline.email"
            clearable
            placeholder="请输入邮箱"
          />
        </el-form-item>
      </re-col>
      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="用户性别">
          <el-select
            v-model="newFormInline.sex"
            placeholder="请选择用户性别"
            class="w-full"
            clearable
          >
            <el-option
              v-for="(item, index) in sexOptions"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </re-col>
      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="用户角色" prop="role">
          <el-select
            v-model="newFormInline.role"
            placeholder="请选择用户角色"
            class="w-full"
            :disabled="newFormInline.isLastSuperAdmin && newFormInline.title === '修改'"
          >
            <el-option
              v-for="(item, index) in roleOptions"
              :key="index"
              :label="item.label"
              :value="item.value"
              :disabled="newFormInline.isLastSuperAdmin && newFormInline.title === '修改' && item.value !== 'SUPER_ADMIN'"
            />
          </el-select>
          <div v-if="newFormInline.isLastSuperAdmin && newFormInline.title === '修改'" class="text-xs text-yellow-600 mt-1">
            不能修改最后一个超级管理员的角色
          </div>
        </el-form-item>
      </re-col>
      <re-col :value="12" :xs="24" :sm="24">
        <el-form-item label="所属部门">
          <el-tree-select
            v-model="newFormInline.departmentId"
            :data="departmentTree"
            :props="departmentProps"
            placeholder="请选择部门"
            class="w-full"
            check-strictly
            :render-after-expand="false"
            clearable
          />
        </el-form-item>
      </re-col>
      <re-col
        v-if="newFormInline.title === '新增'"
        :value="12"
        :xs="24"
        :sm="24"
      >
        <el-form-item label="用户状态">
          <el-switch
            v-model="newFormInline.status"
            inline-prompt
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
            :style="switchStyle"
          />
        </el-form-item>
      </re-col>

      <re-col>
        <el-form-item label="备注">
          <el-input
            v-model="newFormInline.remark"
            placeholder="请输入备注信息"
            type="textarea"
          />
        </el-form-item>
      </re-col>
    </el-row>
  </el-form>
</template>
