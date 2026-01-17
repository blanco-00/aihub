interface FormItemProps {
  id?: number;
  /** 用于判断是`新增`还是`修改` */
  title: string;
  nickname: string;
  username: string;
  password?: string;
  phone: string | number;
  email: string;
  sex: string | number;
  role?: string; // 保留用于向后兼容，主角色
  roleIds?: number[]; // 多角色ID列表
  departmentId?: number;
  status: number;
  remark: string;
  /** 是否是最后一个超级管理员 */
  isLastSuperAdmin?: boolean;
}
interface FormProps {
  formInline: FormItemProps;
}

interface RoleFormItemProps {
  username: string;
  nickname: string;
  /** 角色列表 */
  roleOptions: any[];
  /** 选中的角色列表 */
  ids: number[];
}
interface RoleFormProps {
  formInline: RoleFormItemProps;
}

export type { FormItemProps, FormProps, RoleFormItemProps, RoleFormProps };
