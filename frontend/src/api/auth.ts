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
 * 登录请求参数
 */
export type LoginRequest = {
  usernameOrEmail: string;
  password: string;
  rememberMe?: boolean; // 是否记住我（后端固定30天）
};

/**
 * 登录响应数据（后端实际返回的格式）
 */
export type LoginResponse = {
  token: string; // 后端返回的是 token，不是 accessToken
  refreshToken: string;
  expiresIn: number; // 后端返回的是 expiresIn（秒数），不是 expires（日期）
  user: {
    id: number;
    username: string;
    nickname?: string;
    email: string;
    role: string;
    roleDescription: string;
  };
};

/**
 * 刷新Token请求参数
 */
export type RefreshTokenRequest = {
  refreshToken: string;
};

/**
 * 注册请求参数
 */
export type RegisterRequest = {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
};

/**
 * 忘记密码请求参数
 */
export type ForgotPasswordRequest = {
  email: string;
  code: string;
};

/**
 * 忘记密码响应数据
 */
export type ForgotPasswordResponse = {
  code: string;
  message?: string;
};

/**
 * 重置密码请求参数
 */
export type ResetPasswordRequest = {
  email: string;
  code: string;
  newPassword: string;
  confirmPassword: string;
};

/**
 * 用户登录
 */
export const login = (data: LoginRequest) => {
  return http.request<Result<LoginResponse>>("post", "/api/auth/login", {
    data,
  });
};

/**
 * 刷新Token
 */
export const refreshToken = (data: RefreshTokenRequest) => {
  return http.request<Result<LoginResponse>>("post", "/api/auth/refresh", {
    data,
  });
};

/**
 * 用户登出
 */
export const logout = () => {
  return http.request<Result<void>>("post", "/api/auth/logout");
};

/**
 * 验证Token
 */
export const validateToken = () => {
  return http.request<Result<boolean>>("get", "/api/auth/validate");
};

/**
 * 用户注册
 */
export const register = (data: RegisterRequest) => {
  return http.request<Result<void>>("post", "/api/auth/register", { data });
};

/**
 * 发送忘记密码验证码
 */
export const forgotPassword = (data: ForgotPasswordRequest) => {
  return http.request<Result<ForgotPasswordResponse>>(
    "post",
    "/api/auth/forgot-password",
    { data },
  );
};

/**
 * 重置密码
 */
export const resetPassword = (data: ResetPasswordRequest) => {
  return http.request<Result<void>>("post", "/api/auth/reset-password", {
    data,
  });
};
