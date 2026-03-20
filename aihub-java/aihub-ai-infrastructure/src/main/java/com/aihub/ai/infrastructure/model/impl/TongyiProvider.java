package com.aihub.ai.infrastructure.model.impl;

import com.aihub.ai.infrastructure.model.ModelProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TongyiProvider implements ModelProvider {

    private final RestTemplate restTemplate;

    public TongyiProvider() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getProviderName() {
        return "Tongyi";
    }

    @Override
    public String chat(String modelId, String apiKey, String baseUrl, Map<String, String> messages) {
        String url = buildUrl(baseUrl, "/v1/chat/completions");
        
        String requestBody = String.format("""
            {
                "model": "%s",
                "messages": [
                    {"role": "user", "content": "%s"}
                ]
            }
            """, modelId, escapeJson(messages.get("content")));

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);
            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            return extractContent(response.getBody());
        } catch (Exception e) {
            log.error("Tongyi chat error: {}", e.getMessage(), e);
            throw new RuntimeException("Chat failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void chatStream(String modelId, String apiKey, String baseUrl, Map<String, String> messages, StreamCallback onChunk) {
        onChunk.onError(new RuntimeException("Stream not implemented"));
    }

    @Override
    public boolean healthCheck(String modelId, String apiKey, String baseUrl) {
        try {
            chat(modelId, apiKey, baseUrl, Map.of("content", "ping"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> getModels(String apiKey, String baseUrl) {
        return List.of("qwen-turbo", "qwen-plus", "qwen-max");
    }

    private String buildUrl(String baseUrl, String path) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "https://dashscope.aliyuncs.com/api/v1";
        }
        return baseUrl.endsWith("/") ? baseUrl + path : baseUrl + path;
    }

    private String extractContent(String body) {
        int start = body.indexOf("\"content\":\"");
        if (start == -1) return body;
        start += 11;
        int end = body.indexOf("\"", start);
        return end == -1 ? body : body.substring(start, end);
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
