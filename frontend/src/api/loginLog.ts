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
 * 登录日志信息
 */
export type LoginLogInfo = {
  id: number;
  userId?: number;
  username?: string;
  ip?: string;
  address?: string;
  userAgent?: string;
  status: number;
  message?: string;
  loginTime: string;
};

/**
 * 登录日志列表查询请求参数
 */
export type LoginLogListRequest = {
  current?: number;
  size?: number;
  username?: string;
  ip?: string;
  status?: number;
  startTime?: string;
  endTime?: string;
};

/**
 * 获取登录日志列表
 */
export const getLoginLogList = (params: LoginLogListRequest) => {
  return http.request<Result<{
    records: LoginLogInfo[];
    total: number;
    current: number;
    size: number;
    pages: number;
  }>>("get", "/api/login-logs", { params });
};
