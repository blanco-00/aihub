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
 * 数据库状态
 */
export type DatabaseStatus = {
  connected: boolean;
  databaseExists: boolean;
  tablesInitialized: boolean;
  errorMessage?: string;
};

/**
 * 初始化超级管理员请求参数
 */
export type InitSuperAdminRequest = {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
};

/**
 * 检查系统是否已初始化
 */
export const getInitStatus = () => {
  return http.request<Result<boolean>>("get", "/api/init/status");
};

/**
 * 检查数据库状态
 */
export const getDatabaseStatus = () => {
  return http.request<Result<DatabaseStatus>>("get", "/api/init/database/status");
};

/**
 * 初始化数据库表结构
 */
export const initializeDatabase = () => {
  return http.request<Result<void>>("post", "/api/init/database/init");
};

/**
 * 创建超级管理员
 */
export const createSuperAdmin = (data: InitSuperAdminRequest) => {
  // 只传必要字段，不传 confirmPassword
  const { confirmPassword, ...requestData } = data;
  return http.request<Result<void>>("post", "/api/init/super-admin", { data: requestData });
};
