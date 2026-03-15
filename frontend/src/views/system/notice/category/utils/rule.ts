import { reactive } from "vue";
import type { FormRules } from "element-plus";

export const formRules = reactive<FormRules>({
  name: [{ required: true, message: "分类名称为必填项", trigger: "blur" }],
  code: [{ required: true, message: "分类代码为必填项", trigger: "blur" }],
});
