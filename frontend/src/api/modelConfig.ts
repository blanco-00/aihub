import { http } from "@/utils/http";

export type ModelConfig = {
  id: number;
  name: string;
  vendor: string;
  modelId: string;
  apiKey: string;
  baseUrl?: string;
  status: number;
  config?: string;
  createdAt: string;
  updatedAt: string;
};

export type ModelConfigListParams = {
  current?: number;
  size?: number;
  keyword?: string;
  vendor?: string;
  status?: number;
};

export type CreateModelConfigRequest = {
  name: string;
  vendor: string;
  modelId: string;
  apiKey: string;
  baseUrl?: string;
  status?: number;
  config?: string;
};

export type UpdateModelConfigRequest = {
  name: string;
  vendor: string;
  modelId: string;
  apiKey: string;
  baseUrl?: string;
  status?: number;
  config?: string;
};

export const getModelConfigList = (params: ModelConfigListParams) => {
  return http.request<any>("get", "/api/model-configs", { params });
};

export const getModelConfigById = (id: number) => {
  return http.request<any>("get", `/api/model-configs/${id}`);
};

export const getEnabledModelConfigs = () => {
  return http.request<any>("get", "/api/model-configs/enabled");
};

export const createModelConfig = (data: CreateModelConfigRequest) => {
  return http.request<any>("post", "/api/model-configs", { data });
};

export const updateModelConfig = (
  id: number,
  data: UpdateModelConfigRequest,
) => {
  return http.request<any>("put", `/api/model-configs/${id}`, { data });
};

export const deleteModelConfig = (id: number) => {
  return http.request<any>("delete", `/api/model-configs/${id}`);
};

export const toggleModelConfigStatus = (id: number, status: number) => {
  return http.request<any>("put", `/api/model-configs/${id}/status`, {
    params: { status },
  });
};

export const setDefaultModel = (id: number) => {
  return http.request<any>("put", `/api/model-configs/${id}/default`);
};

export const getDefaultModel = () => {
  return http.request<any>("get", "/api/model-configs/default");
};

export const chatWithModel = (modelId: number, message: string) => {
  return http.request<any>(
    "post",
    `/api/ai/chat?modelId=${modelId}&message=${encodeURIComponent(message)}`,
  );
};

export const checkModelHealth = (modelId: number) => {
  return http.request<any>("get", `/api/ai/chat/health?modelId=${modelId}`);
};

export type ModelTestResult = {
  modelId: number;
  modelName: string;
  vendor: string;
  userMessage: string;
  response?: string;
  success: boolean;
  error?: string;
  responseTimeMs: number;
  timestamp: string;
};

export type MultiModelComparisonRequest = {
  modelIds: number[];
  message: string;
};

export type MultiModelComparisonResponse = {
  results: ModelTestResult[];
  totalModels: number;
  successCount: number;
  failureCount: number;
};

export const compareModels = (data: MultiModelComparisonRequest) => {
  return http.request<MultiModelComparisonResponse>(
    "post",
    "/api/ai/chat/compare",
    { data },
  );
};

export const getTestHistory = (params: {
  modelId?: number;
  keyword?: string;
  current?: number;
  size?: number;
}) => {
  return http.request<any>("get", "/api/ai/chat/history", { params });
};

export const getModelList = (
  vendor: string,
  apiKey: string,
  baseUrl?: string,
) => {
  return http.request<any>("get", "/api/ai/chat/models", {
    params: { vendor, apiKey, baseUrl },
  });
};
