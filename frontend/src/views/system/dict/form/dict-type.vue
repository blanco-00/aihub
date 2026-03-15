<script setup lang="ts">
import { ref } from "vue";
import { dictTypeFormRules } from "../utils/rule";
import type { DictTypeFormItemProps } from "../utils/types";

interface FormProps {
  formInline: DictTypeFormItemProps;
}

const props = withDefaults(defineProps<FormProps>(), {
  formInline: () => ({
    dictName: "",
    dictType: "",
    status: 1,
    remark: "",
  }),
});

const ruleFormRef = ref();
const newFormInline = ref(props.formInline);

function getRef() {
  return ruleFormRef.value;
}

defineExpose({ getRef });
</script>

<template>
  <el-form
    ref="ruleFormRef"
    :model="newFormInline"
    :rules="dictTypeFormRules"
    label-width="100px"
  >
    <el-form-item label="字典名称" prop="dictName">
      <el-input
        v-model="newFormInline.dictName"
        clearable
        placeholder="请输入字典名称"
      />
    </el-form-item>

    <el-form-item label="字典类型" prop="dictType">
      <el-input
        v-model="newFormInline.dictType"
        clearable
        placeholder="请输入字典类型（系统编码）"
      />
      <div class="text-xs text-gray-500 mt-1">
        只能包含小写字母、数字和下划线，且必须以字母开头
      </div>
    </el-form-item>

    <el-form-item label="状态" prop="status">
      <el-radio-group v-model="newFormInline.status">
        <el-radio :value="1">正常</el-radio>
        <el-radio :value="0">停用</el-radio>
      </el-radio-group>
    </el-form-item>

    <el-form-item label="备注" prop="remark">
      <el-input
        v-model="newFormInline.remark"
        placeholder="请输入备注信息"
        type="textarea"
        :rows="3"
        maxlength="500"
        show-word-limit
      />
    </el-form-item>
  </el-form>
</template>
