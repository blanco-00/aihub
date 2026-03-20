package com.aihub.ai.infrastructure.model.impl;

import com.aihub.ai.infrastructure.model.ModelProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAIProvider implements ModelProvider {

    private final RestTemplate restTemplate;

    public OpenAIProvider() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getProviderName() {
        return "OpenAI";
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
            log.error("OpenAI chat error: {}", e.getMessage(), e);
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
            chat("gpt-3.5-turbo", apiKey, baseUrl, Map.of("content", "ping"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> getModels(String apiKey, String baseUrl) {
        String url = buildUrl(baseUrl, "/v1/models");
        
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
            
            return parseModelIds(response.getBody());
        } catch (Exception e) {
            log.error("OpenAI getModels error: {}", e.getMessage(), e);
            return List.of("gpt-4", "gpt-4-turbo", "gpt-3.5-turbo");
        }
    }

    private String buildUrl(String baseUrl, String path) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "https://api.openai.com";
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
        List<String> models = new java.util.ArrayList<>();
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
                if (idStart == -1) continue;
                
                idStart += 6;
                int idEnd = obj.indexOf("\"", idStart);
                if (idEnd == -1) continue;
                
                models.add(obj.substring(idStart, idEnd));
            }
        } catch (Exception e) {
            log.warn("Failed to parse models response: {}", e.getMessage());
        }
        
        if (models.isEmpty()) {
            models.add("gpt-4");
            models.add("gpt-4-turbo");
            models.add("gpt-3.5-turbo");
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
