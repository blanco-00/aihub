import { http } from "@/utils/http";
import { getToken } from "@/utils/auth";

export type MCPTool = {
  name: string;
  description: string;
  inputSchema: Record<string, any>;
  outputSchema?: Record<string, any>;
  tool_type?: string;
  is_enabled?: number;
  execution_count?: number;
  last_executed_at?: string;
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

const PYTHON_API_BASE = "http://localhost:9529";

export const getMCPTools = (): Promise<any> => {
  return fetch(`${PYTHON_API_BASE}/api/mcp/tools`).then((res) => res.json());
};

export const executeMCPTool = (data: ToolExecutionRequest): Promise<any> => {
  return fetch(`${PYTHON_API_BASE}/api/mcp/tools/execute`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  }).then((res) => res.json());
};

export type MCPToolUpdateRequest = {
  name: string;
  isEnabled?: number;
};

export const updateMCPTool = (data: MCPToolUpdateRequest): Promise<any> => {
  return fetch(`${PYTHON_API_BASE}/api/mcp/tools/update`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  }).then((res) => res.json());
};

export const getToolExecutionLogs = (): Promise<any> => {
  return fetch(`${PYTHON_API_BASE}/api/mcp/tools/logs`).then((res) => res.json());
};
