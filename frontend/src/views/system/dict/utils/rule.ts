import { reactive } from "vue";
import type { FormRules } from "element-plus";
import type { DictTypeFormItemProps, DictDataFormItemProps } from "./types";

/** 字典类型表单校验规则 */
export const dictTypeFormRules = reactive<FormRules<DictTypeFormItemProps>>({
  dictName: [
    { required: true, message: "字典名称不能为空", trigger: "blur" },
    { max: 100, message: "字典名称长度不能超过100个字符", trigger: "blur" }
  ],
  dictType: [
    { required: true, message: "字典类型不能为空", trigger: "blur" },
    { max: 100, message: "字典类型长度不能超过100个字符", trigger: "blur" },
    {
      pattern: /^[a-z][a-z0-9_]*$/,
      message: "字典类型只能包含小写字母、数字和下划线，且必须以字母开头",
      trigger: "blur"
    }
  ],
  remark: [
    { max: 500, message: "备注长度不能超过500个字符", trigger: "blur" }
  ]
});

/** 字典数据表单校验规则 */
export const dictDataFormRules = reactive<FormRules<DictDataFormItemProps>>({
  dictType: [
    { required: true, message: "字典类型不能为空", trigger: "blur" }
  ],
  dictLabel: [
    { required: true, message: "字典标签不能为空", trigger: "blur" },
    { max: 100, message: "字典标签长度不能超过100个字符", trigger: "blur" }
  ],
  dictValue: [
    { required: true, message: "字典键值不能为空", trigger: "blur" },
    { max: 100, message: "字典键值长度不能超过100个字符", trigger: "blur" }
  ],
  sortOrder: [
    {
      validator: (rule: any, value: any, callback: any) => {
        // 允许 null 或 undefined（el-input-number 可能返回这些值）
        if (value === null || value === undefined) {
          callback();
          return;
        }
        // 检查是否为数字
        if (typeof value !== "number" || isNaN(value)) {
          callback(new Error("排序必须为数字"));
          return;
        }
        // 检查是否小于0
        if (value < 0) {
          callback(new Error("排序不能小于0"));
          return;
        }
        callback();
      },
      trigger: "blur"
    }
  ],
  remark: [
    { max: 500, message: "备注长度不能超过500个字符", trigger: "blur" }
  ]
});
