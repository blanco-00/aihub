package com.aihub.ai.infrastructure;

import com.aihub.ai.infrastructure.model.ModelProvider;
import com.aihub.ai.infrastructure.model.ModelProviderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ModelGateway {

    @Autowired
    private ModelProviderFactory providerFactory;

    public String chat(String vendor, String modelId, String apiKey, String baseUrl, String userMessage) {
        ModelProvider provider = providerFactory.getProvider(vendor);
        
        Map<String, String> messages = new HashMap<>();
        messages.put("content", userMessage);

        return provider.chat(modelId, apiKey, baseUrl, messages);
    }

    public void chatStream(String vendor, String modelId, String apiKey, String baseUrl, String userMessage, ModelProvider.StreamCallback callback) {
        ModelProvider provider = providerFactory.getProvider(vendor);
        
        Map<String, String> messages = new HashMap<>();
        messages.put("content", userMessage);

        provider.chatStream(modelId, apiKey, baseUrl, messages, callback);
    }

    public boolean healthCheck(String vendor, String modelId, String apiKey, String baseUrl) {
        try {
            ModelProvider provider = providerFactory.getProvider(vendor);
            return provider.healthCheck(modelId, apiKey, baseUrl);
        } catch (Exception e) {
            log.error("Health check failed for vendor {}: {}", vendor, e.getMessage());
            return false;
        }
    }

    public List<String> getModels(String vendor, String apiKey, String baseUrl) {
        log.info("ModelGateway getModels: vendor={}, baseUrl={}", vendor, baseUrl);
        ModelProvider provider = providerFactory.getProvider(vendor);
        log.info("Found provider: {}", provider.getProviderName());
        return provider.getModels(apiKey, baseUrl);
    }
}
