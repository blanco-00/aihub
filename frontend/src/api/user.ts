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
 * 分页结果
 */
export type PageResult<T> = {
  records: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
};

/**
 * 用户信息
 */
export type UserInfo = {
  id: number;
  username: string;
  nickname?: string;
  email: string;
  phone?: string;
  role: string;
  roleDescription: string;
  status: number;
  createdAt: string;
  updatedAt: string;
};

/**
 * 用户列表查询参数
 */
export type UserListParams = {
  current?: number;
  size?: number;
  keyword?: string;
  phone?: string;
  role?: string;
  status?: number;
};

/**
 * 创建用户请求参数
 */
export type CreateUserRequest = {
  username: string;
  nickname?: string;
  email: string;
  phone?: string;
  password: string;
  role: string;
  status?: number;
};

/**
 * 更新用户请求参数
 */
export type UpdateUserRequest = {
  username: string;
  nickname?: string;
  email: string;
  phone?: string;
  role: string;
  status?: number;
  password?: string;
};

/**
 * 获取用户列表（分页、搜索、筛选）
 */
export const getUserList = (params?: UserListParams) => {
  return http.request<Result<PageResult<UserInfo>>>("get", "/api/users", { params });
};

/**
 * 根据ID获取用户详情
 */
export const getUserById = (id: number) => {
  return http.request<Result<UserInfo>>("get", `/api/users/${id}`);
};

/**
 * 创建用户
 */
export const createUser = (data: CreateUserRequest) => {
  return http.request<Result<void>>("post", "/api/users", { data });
};

/**
 * 更新用户
 */
export const updateUser = (id: number, data: UpdateUserRequest) => {
  return http.request<Result<void>>("put", `/api/users/${id}`, { data });
};

/**
 * 删除用户（逻辑删除）
 */
export const deleteUser = (id: number) => {
  return http.request<Result<void>>("delete", `/api/users/${id}`);
};

/**
 * 启用/禁用用户
 */
export const toggleUserStatus = (id: number, status: number) => {
  return http.request<Result<void>>("put", `/api/users/${id}/status`, { params: { status } });
};

// 以下为兼容旧代码的接口（已废弃，建议使用 auth.ts 中的接口）
export type UserResult = {
  code: number;
  message: string;
  data: {
    /** 头像 */
    avatar: string;
    /** 用户名 */
    username: string;
    /** 昵称 */
    nickname: string;
    /** 当前登录用户的角色 */
    roles: Array<string>;
    /** 按钮级别权限 */
    permissions: Array<string>;
    /** `token` */
    accessToken: string;
    /** 用于调用刷新`accessToken`的接口时所需的`token` */
    refreshToken: string;
    /** `accessToken`的过期时间（格式'xxxx/xx/xx xx:xx:xx'） */
    expires: Date;
  };
};

export type RefreshTokenResult = {
  code: number;
  message: string;
  data: {
    /** `token` */
    accessToken: string;
    /** 用于调用刷新`accessToken`的接口时所需的`token` */
    refreshToken: string;
    /** `accessToken`的过期时间（格式'xxxx/xx/xx xx:xx:xx'） */
    expires: Date;
  };
};

type ResultTable = {
  code: number;
  message: string;
  data?: {
    /** 列表数据 */
    list: Array<any>;
    /** 总条目数 */
    total?: number;
    /** 每页显示条目个数 */
    pageSize?: number;
    /** 当前页数 */
    currentPage?: number;
  };
};

/** 登录（已废弃，请使用 auth.ts 中的 login） */
export const getLogin = (data?: object) => {
  return http.request<UserResult>("post", "/api/auth/login", { data });
};

/** 刷新`token`（已废弃，请使用 auth.ts 中的 refreshToken） */
export const refreshTokenApi = (data?: object) => {
  return http.request<RefreshTokenResult>("post", "/api/auth/refresh", { data });
};

/** 账户设置-个人信息 */
export const getMine = (data?: object) => {
  return http.request<Result<UserInfo>>("get", "/api/auth/me", { data }).then((response: any) => {
    // 转换后端数据格式为前端需要的格式
    if (response.code === 200 && response.data) {
      const userData = response.data;
      return {
        code: 0,
        message: response.message || "success",
        data: {
          id: userData.id,
          username: userData.username,
          nickname: userData.nickname || userData.username, // 使用后端返回的nickname，如果没有则使用username
          email: userData.email,
          phone: userData.phone || "",
          avatar: "", // 后端没有avatar字段
          description: "", // 后端没有description字段
          role: userData.role,
          roleDescription: userData.roleDescription,
          status: userData.status
        }
      };
    }
    return response;
  });
};

/** 账户设置-个人安全日志 */
export const getMineLogs = (data?: object) => {
  return http.request<ResultTable>("get", "/mine-logs", { data });
};
