import { http } from "@/utils/http";

/**
 * 统一响应格式
 */
type Result<T = any> = {
  code: number;
  message: string;
  data: T;
};

/**
 * 部门信息
 */
export type DepartmentInfo = {
  id: number;
  name: string;
  parentId: number;
  sortOrder: number;
  status: number;
  remark?: string;
  createdAt?: string;
  updatedAt?: string;
  children?: DepartmentInfo[];
};

/**
 * 创建部门请求参数
 */
export type CreateDepartmentRequest = {
  name: string;
  parentId?: number;
  sortOrder?: number;
  status?: number;
  remark?: string;
};

/**
 * 更新部门请求参数
 */
export type UpdateDepartmentRequest = {
  name?: string;
  parentId?: number;
  sortOrder?: number;
  status?: number;
  remark?: string;
};

/**
 * 获取所有部门列表（扁平结构）
 */
export const getDeptList = () => {
  return http.request<Result<DepartmentInfo[]>>("get", "/api/departments");
};

/**
 * 获取部门树
 */
export const getDepartmentTree = () => {
  return http.request<Result<DepartmentInfo[]>>("get", "/api/departments/tree");
};

/**
 * 根据ID获取部门详情
 */
export const getDepartmentById = (id: number) => {
  return http.request<Result<DepartmentInfo>>("get", `/api/departments/${id}`);
};

/**
 * 创建部门
 */
export const createDepartment = (data: CreateDepartmentRequest) => {
  return http.request<Result<void>>("post", "/api/departments", { data });
};

/**
 * 更新部门
 */
export const updateDepartment = (id: number, data: UpdateDepartmentRequest) => {
  return http.request<Result<void>>("put", `/api/departments/${id}`, { data });
};

/**
 * 删除部门（逻辑删除）
 */
export const deleteDepartment = (id: number) => {
  return http.request<Result<void>>("delete", `/api/departments/${id}`);
};
