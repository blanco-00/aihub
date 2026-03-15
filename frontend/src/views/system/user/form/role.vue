<script setup lang="ts">
import { ref, watch } from "vue";
import ReCol from "@/components/ReCol";
import { RoleFormProps } from "../utils/types";

const props = withDefaults(defineProps<RoleFormProps>(), {
  formInline: () => ({
    username: "",
    nickname: "",
    roleOptions: [],
    ids: [],
  }),
});

const newFormInline = ref(props.formInline);

// 监听 formInline 变化，同步到 newFormInline
watch(
  () => props.formInline,
  (newVal) => {
    newFormInline.value = { ...newVal };
  },
  { deep: true, immediate: true },
);

function getFormData() {
  return newFormInline.value;
}

defineExpose({ getFormData });
</script>

<template>
  <el-form :model="newFormInline">
    <el-row :gutter="30">
      <!-- <re-col>
        <el-form-item label="用户名称" prop="username">
          <el-input disabled v-model="newFormInline.username" />
        </el-form-item>
      </re-col> -->
      <re-col>
        <el-form-item label="用户昵称" prop="nickname">
          <el-input v-model="newFormInline.nickname" disabled />
        </el-form-item>
      </re-col>
      <re-col>
        <el-form-item label="角色列表" prop="ids">
          <el-select
            v-model="newFormInline.ids"
            placeholder="请选择"
            class="w-full"
            clearable
            multiple
          >
            <el-option
              v-for="(item, index) in newFormInline.roleOptions"
              :key="index"
              :value="item.id"
              :label="item.name"
            >
              {{ item.name }}
            </el-option>
          </el-select>
        </el-form-item>
      </re-col>
    </el-row>
  </el-form>
</template>
