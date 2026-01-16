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
 * 菜单信息
 */
export type MenuInfo = {
  id: number;
  parentId: number;
  name: string;
  path: string;
  component?: string;
  redirect?: string;
  icon?: string;
  title: string;
  sortOrder: number;
  showLink: number;
  keepAlive: number;
  status: number;
  createdAt?: string;
  updatedAt?: string;
  children?: MenuInfo[];
};

/**
 * 创建菜单请求参数
 */
export type CreateMenuRequest = {
  parentId?: number;
  name: string;
  path: string;
  component?: string;
  redirect?: string;
  icon?: string;
  title: string;
  sortOrder?: number;
  showLink?: number;
  keepAlive?: number;
  status?: number;
};

/**
 * 更新菜单请求参数
 */
export type UpdateMenuRequest = {
  parentId?: number;
  name?: string;
  path?: string;
  component?: string;
  redirect?: string;
  icon?: string;
  title?: string;
  sortOrder?: number;
  showLink?: number;
  keepAlive?: number;
  status?: number;
};

/**
 * 获取所有菜单树（用于菜单管理页面，不根据角色过滤）
 */
export const getMenuTree = () => {
  return http.request<Result<MenuInfo[]>>("get", "/api/menus/tree/all");
};

/**
 * 获取菜单树（根据用户角色，用于动态路由）
 */
export const getMenuTreeByRole = () => {
  return http.request<Result<any[]>>("get", "/api/menus/tree");
};

/**
 * 根据ID获取菜单详情
 */
export const getMenuById = (id: number) => {
  return http.request<Result<MenuInfo>>("get", `/api/menus/${id}`);
};

/**
 * 创建菜单
 */
export const createMenu = (data: CreateMenuRequest) => {
  return http.request<Result<void>>("post", "/api/menus", { data });
};

/**
 * 更新菜单
 */
export const updateMenu = (id: number, data: UpdateMenuRequest) => {
  return http.request<Result<void>>("put", `/api/menus/${id}`, { data });
};

/**
 * 删除菜单（逻辑删除）
 */
export const deleteMenu = (id: number) => {
  return http.request<Result<void>>("delete", `/api/menus/${id}`);
};
