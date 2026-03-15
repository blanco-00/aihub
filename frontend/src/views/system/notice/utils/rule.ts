import { reactive } from "vue";
import type { FormRules } from "element-plus";

export const formRules = reactive<FormRules>({
  title: [{ required: true, message: "通知标题为必填项", trigger: "blur" }],
  content: [{ required: true, message: "通知内容为必填项", trigger: "blur" }],
});
