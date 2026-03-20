package com.aihub.ai.infrastructure.model;

import java.util.List;
import java.util.Map;

public interface ModelProvider {

    String getProviderName();

    String chat(String modelId, String apiKey, String baseUrl, Map<String, String> messages);

    void chatStream(String modelId, String apiKey, String baseUrl, Map<String, String> messages, StreamCallback onChunk);

    boolean healthCheck(String modelId, String apiKey, String baseUrl);

    /**
     * 获取可用的模型列表
     * @param apiKey API密钥
     * @param baseUrl 基础URL
     * @return 模型ID列表
     */
    List<String> getModels(String apiKey, String baseUrl);

    interface StreamCallback {
        void onChunk(String chunk);
        void onComplete();
        void onError(Exception e);
    }
}
