package com.aihub.ai.infrastructure.model.impl;

import com.aihub.ai.infrastructure.model.ModelProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ZhipuAIProvider implements ModelProvider {

    private final RestTemplate restTemplate;

    public ZhipuAIProvider() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getProviderName() {
        return "ZhipuAI";
    }

    @Override
    public String chat(String modelId, String apiKey, String baseUrl, Map<String, String> messages) {
        String url = buildUrl(baseUrl, "/v4/chat/completions");
        
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
            log.error("ZhipuAI chat error: {}", e.getMessage(), e);
            throw new RuntimeException("Chat failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void chatStream(String modelId, String apiKey, String baseUrl, Map<String, String> messages, StreamCallback onChunk) {
        onChunk.onError(new RuntimeException("Stream not implemented"));
    }

    @Override
    public boolean healthCheck(String apiKey, String baseUrl) {
        try {
            chat("glm-4", apiKey, baseUrl, Map.of("content", "ping"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> getModels(String apiKey, String baseUrl) {
        String url = buildUrl(baseUrl, "/models");
        log.info("ZhipuAI getModels: url={}", url);
        
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                url, 
                org.springframework.http.HttpMethod.GET, 
                entity, 
                String.class
            );
            
            log.info("ZhipuAI getModels response: {}", response.getBody());
            return parseModelIds(response.getBody());
        } catch (Exception e) {
            log.error("ZhipuAI getModels error: {}", e.getMessage(), e);
            return List.of("glm-4", "glm-4-flash", "glm-4-plus");
        }
    }

    private String buildUrl(String baseUrl, String path) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "https://open.bigmodel.cn/api/paas/v4";
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

    private List<String> parseModelIds(String body) {
        List<String> models = new ArrayList<>();
        if (body == null || body.isEmpty()) {
            return models;
        }
        
        try {
            int dataStart = body.indexOf("\"data\":[");
            if (dataStart == -1) {
                return models;
            }
            
            int arrayStart = body.indexOf("[", dataStart);
            int arrayEnd = body.indexOf("]", arrayStart);
            if (arrayStart == -1 || arrayEnd == -1) {
                return models;
            }
            
            String arrayContent = body.substring(arrayStart + 1, arrayEnd);
            String[] objects = arrayContent.split("\\},\\{");
            
            for (String obj : objects) {
                int idStart = obj.indexOf("\"id\":\"");
                if (idStart == -1) {
                    idStart = obj.indexOf("'id':'");
                }
                if (idStart == -1) continue;
                
                idStart += 6;
                int idEnd = obj.indexOf("\"", idStart);
                if (idEnd == -1) {
                    idEnd = obj.indexOf("'", idStart);
                }
                if (idEnd == -1) continue;
                
                models.add(obj.substring(idStart, idEnd));
            }
        } catch (Exception e) {
            log.warn("Failed to parse models response, using defaults: {}", e.getMessage());
        }
        
        if (models.isEmpty()) {
            models.add("glm-4");
            models.add("glm-4-flash");
            models.add("glm-4-plus");
            models.add("glm-3-turbo");
        }
        
        return models;
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
