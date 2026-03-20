import { http } from "@/utils/http";

type Result<T = any> = {
  code: number;
  message: string;
  data?: T;
};

type PageResult<T = any> = {
  records: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
};

export type PromptTemplate = {
  id: number;
  name: string;
  description: string;
  categoryId: number;
  categoryName: string;
  content: string;
  variables: Record<string, { label: string; defaultValue?: string }>;
  isBuiltin: number;
  status: number;
  createdBy: number;
  createdAt: string;
  updatedAt?: string;
};

export type PromptCategory = {
  id: number;
  name: string;
  code: string;
  description: string;
};

export type CreatePromptTemplateRequest = {
  name: string;
  description: string;
  categoryId: number;
  content: string;
  variables?: Record<string, { label: string; defaultValue?: string }>;
};

export type UpdatePromptTemplateRequest = {
  id: number;
  name: string;
  description: string;
  categoryId: number;
  content: string;
  variables?: Record<string, { label: string; defaultValue?: string }>;
  status: number;
};

export type PromptTemplateListRequest = {
  categoryId?: number;
  keyword?: string;
  current: number;
  size: number;
};

/** 获取Prompt模板列表 */
export const getPromptTemplateList = (params: PromptTemplateListRequest) => {
  return http.request<Result<PageResult<PromptTemplate>>>(
    "get",
    "/api/prompt/list",
    { params },
  );
};

/** 获取单个Prompt模板 */
export const getPromptTemplate = (id: number) => {
  return http.request<Result<PromptTemplate>>("get", `/api/prompt/${id}`);
};

/** 创建Prompt模板 */
export const createPromptTemplate = (data: CreatePromptTemplateRequest) => {
  return http.request<Result<number>>("post", "/api/prompt/create", { data });
};

/** 更新Prompt模板 */
export const updatePromptTemplate = (data: UpdatePromptTemplateRequest) => {
  return http.request<Result<void>>("put", "/api/prompt/update", { data });
};

/** 删除Prompt模板 */
export const deletePromptTemplate = (id: number) => {
  return http.request<Result<void>>("delete", `/api/prompt/${id}`);
};

/** 渲染Prompt模板 */
export const renderPromptTemplate = (
  id: number,
  variables: Record<string, string>,
) => {
  return http.request<Result<string>>("post", `/api/prompt/${id}/render`, {
    data: variables,
  });
};

/** 获取Prompt分类列表 */
export const getPromptCategories = () => {
  return http.request<Result<PromptCategory[]>>(
    "get",
    "/api/prompt/categories",
  );
};

/** 切换模板状态 */
export const togglePromptTemplateStatus = (id: number, status: number) => {
  return http.request<Result<void>>("put", `/api/prompt/${id}/status`, {
    params: { status },
  });
};
