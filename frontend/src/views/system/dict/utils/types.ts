import { FormItemProps } from "@pureadmin/table";

export type DictTypeFormItemProps = {
  id?: number;
  dictName?: string;
  dictType?: string;
  status?: number;
  remark?: string;
};

export type DictDataFormItemProps = {
  dictType?: string;
  dictLabel?: string;
  dictValue?: string;
  sortOrder?: number;
  status?: number;
  remark?: string;
};

export type formProps = FormItemProps;
