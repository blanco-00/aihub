<script setup lang="ts">
import { ref } from "vue";
import { dictDataFormRules } from "../utils/rule";
import type { DictDataFormItemProps } from "../utils/types";

interface FormProps {
  formInline: DictDataFormItemProps;
}

const props = withDefaults(defineProps<FormProps>(), {
  formInline: () => ({
    dictType: "",
    dictLabel: "",
    dictValue: "",
    sortOrder: 0,
    status: 1,
    remark: ""
  })
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
    :rules="dictDataFormRules"
    label-width="100px"
  >
    <el-form-item label="字典类型" prop="dictType">
      <el-input
        v-model="newFormInline.dictType"
        clearable
        placeholder="请输入字典类型"
        :disabled="!!formInline.id"
      />
    </el-form-item>

    <el-form-item label="字典标签" prop="dictLabel">
      <el-input
        v-model="newFormInline.dictLabel"
        clearable
        placeholder="请输入字典标签"
      />
    </el-form-item>

    <el-form-item label="字典键值" prop="dictValue">
      <el-input
        v-model="newFormInline.dictValue"
        clearable
        placeholder="请输入字典键值"
      />
    </el-form-item>

    <el-form-item label="字典排序" prop="sortOrder">
      <el-input-number
        v-model="newFormInline.sortOrder"
        :min="0"
        :precision="0"
        placeholder="请输入字典排序"
        class="w-full"
      />
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
