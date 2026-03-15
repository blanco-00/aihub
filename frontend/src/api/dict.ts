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
 * 字典类型信息
 */
export type DictTypeInfo = {
  id: number;
  dictName: string;
  dictType: string;
  status: number;
  remark?: string;
  createdAt: string;
  updatedAt: string;
};

/**
 * 字典数据信息
 */
export type DictDataInfo = {
  id: number;
  dictType: string;
  dictLabel: string;
  dictValue: string;
  sortOrder: number;
  status: number;
  remark?: string;
  createdAt: string;
  updatedAt: string;
};

/**
 * 字典类型列表查询参数
 */
export type DictTypeListParams = {
  current?: number;
  size?: number;
  dictName?: string;
  dictType?: string;
  status?: number;
  startTime?: string;
  endTime?: string;
};

/**
 * 创建字典类型请求参数
 */
export type CreateDictTypeRequest = {
  dictName: string;
  dictType: string;
  status?: number;
  remark?: string;
};

/**
 * 更新字典类型请求参数
 */
export type UpdateDictTypeRequest = {
  dictName: string;
  dictType: string;
  status?: number;
  remark?: string;
};

/**
 * 创建字典数据请求参数
 */
export type CreateDictDataRequest = {
  dictType: string;
  dictLabel: string;
  dictValue: string;
  sortOrder?: number;
  status?: number;
  remark?: string;
};

/**
 * 更新字典数据请求参数
 */
export type UpdateDictDataRequest = {
  dictType: string;
  dictLabel: string;
  dictValue: string;
  sortOrder?: number;
  status?: number;
  remark?: string;
};

/**
 * 获取字典类型列表（分页、搜索、筛选）
 */
export const getDictTypeList = (params?: DictTypeListParams) => {
  return http.request<Result<PageResult<DictTypeInfo>>>(
    "get",
    "/api/dict-types",
    { params },
  );
};

/**
 * 根据ID获取字典类型详情
 */
export const getDictTypeById = (id: number) => {
  return http.request<Result<DictTypeInfo>>("get", `/api/dict-types/${id}`);
};

/**
 * 创建字典类型
 */
export const createDictType = (data: CreateDictTypeRequest) => {
  return http.request<Result<void>>("post", "/api/dict-types", { data });
};

/**
 * 更新字典类型
 */
export const updateDictType = (id: number, data: UpdateDictTypeRequest) => {
  return http.request<Result<void>>("put", `/api/dict-types/${id}`, { data });
};

/**
 * 删除字典类型（逻辑删除）
 */
export const deleteDictType = (id: number) => {
  return http.request<Result<void>>("delete", `/api/dict-types/${id}`);
};

/**
 * 刷新字典缓存
 */
export const refreshDictCache = () => {
  return http.request<Result<void>>("post", "/api/dict-types/refresh-cache");
};

/**
 * 根据字典类型获取字典数据列表（分页）
 */
export const getDictDataList = (
  dictType: string,
  params?: { current?: number; size?: number },
) => {
  return http.request<Result<PageResult<DictDataInfo>>>(
    "get",
    "/api/dict-data",
    {
      params: { dictType, ...params },
    },
  );
};

/**
 * 根据ID获取字典数据详情
 */
export const getDictDataById = (id: number) => {
  return http.request<Result<DictDataInfo>>("get", `/api/dict-data/${id}`);
};

/**
 * 创建字典数据
 */
export const createDictData = (data: CreateDictDataRequest) => {
  return http.request<Result<void>>("post", "/api/dict-data", { data });
};

/**
 * 更新字典数据
 */
export const updateDictData = (id: number, data: UpdateDictDataRequest) => {
  return http.request<Result<void>>("put", `/api/dict-data/${id}`, { data });
};

/**
 * 删除字典数据（逻辑删除）
 */
export const deleteDictData = (id: number) => {
  return http.request<Result<void>>("delete", `/api/dict-data/${id}`);
};
