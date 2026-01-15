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
 * 角色信息
 */
export type RoleInfo = {
  id: number;
  code: string;
  name: string;
  description?: string;
  status: number;
  createdAt?: string;
  updatedAt?: string;
};

/**
 * 创建角色请求参数
 */
export type CreateRoleRequest = {
  code: string;
  name: string;
  description?: string;
  status?: number;
};

/**
 * 更新角色请求参数
 */
export type UpdateRoleRequest = {
  name?: string;
  description?: string;
  status?: number;
};

/**
 * 获取所有角色列表
 */
export const getAllRoles = () => {
  return http.request<Result<RoleInfo[]>>("get", "/api/roles");
};

/**
 * 根据ID获取角色详情
 */
export const getRoleById = (id: number) => {
  return http.request<Result<RoleInfo>>("get", `/api/roles/${id}`);
};

/**
 * 创建角色
 */
export const createRole = (data: CreateRoleRequest) => {
  return http.request<Result<void>>("post", "/api/roles", { data });
};

/**
 * 更新角色
 */
export const updateRole = (id: number, data: UpdateRoleRequest) => {
  return http.request<Result<void>>("put", `/api/roles/${id}`, { data });
};

/**
 * 删除角色（逻辑删除）
 */
export const deleteRole = (id: number) => {
  return http.request<Result<void>>("delete", `/api/roles/${id}`);
};
