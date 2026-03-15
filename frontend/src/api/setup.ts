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
 * 数据库配置
 */
export type DatabaseConfig = {
  host: string;
  port: number;
  database: string;
  username: string;
  password: string;
};

/**
 * 连接测试结果
 */
export type ConnectionTestResult = {
  success: boolean;
  errorMessage?: string;
  databaseExists: boolean;
};

/**
 * 检查是否已配置数据库
 */
export const getSetupStatus = () => {
  return http.request<Result<boolean>>("get", "/api/setup/status");
};

/**
 * 测试数据库连接
 */
export const testConnection = (data: DatabaseConfig) => {
  return http.request<Result<ConnectionTestResult>>(
    "post",
    "/api/setup/test-connection",
    { data },
  );
};

/**
 * 保存数据库配置
 */
export const saveConfig = (data: DatabaseConfig) => {
  return http.request<Result<void>>("post", "/api/setup/save-config", { data });
};
