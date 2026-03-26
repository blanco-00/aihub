import { http } from "@/utils/http";
import { getToken } from "@/utils/auth";

export type MCPTool = {
  name: string;
  description: string;
  inputSchema: Record<string, any>;
  outputSchema?: Record<string, any>;
  toolType?: string;
};

export type ToolExecutionRequest = {
  toolName: string;
  arguments: Record<string, any>;
};

export type ToolExecutionResult = {
  success: boolean;
  result?: string;
  error?: string;
  executionMs?: number;
};

export const getMCPTools = (): Promise<any> => {
  const token = getToken();
  const tokenStr = token?.accessToken ? `Bearer ${token.accessToken}` : "";

  return fetch("http://localhost:8001/api/mcp/tools", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: tokenStr,
    },
  }).then((res) => res.json());
};

export const executeMCPTool = (data: ToolExecutionRequest): Promise<any> => {
  const token = getToken();
  const tokenStr = token?.accessToken ? `Bearer ${token.accessToken}` : "";

  return fetch("http://localhost:8001/api/mcp/tools/execute", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: tokenStr,
    },
    body: JSON.stringify(data),
  }).then((res) => res.json());
};

export const getToolExecutionLogs = (): Promise<any> => {
  const token = getToken();
  const tokenStr = token?.accessToken ? `Bearer ${token.accessToken}` : "";

  return fetch("http://localhost:8001/api/mcp/tools/logs", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: tokenStr,
    },
  }).then((res) => res.json());
};
