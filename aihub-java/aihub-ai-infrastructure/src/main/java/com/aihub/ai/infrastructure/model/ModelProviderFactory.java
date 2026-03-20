package com.aihub.ai.infrastructure.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ModelProviderFactory {

    private final Map<String, ModelProvider> providers = new HashMap<>();
    private final Map<String, String> vendorAlias = new HashMap<>();

    public ModelProviderFactory(List<ModelProvider> providerList) {
        for (ModelProvider provider : providerList) {
            providers.put(provider.getProviderName(), provider);
        }
        vendorAlias.put("openai", "OpenAI");
        vendorAlias.put("anthropic", "Anthropic");
        vendorAlias.put("zhipuai", "ZhipuAI");
        vendorAlias.put("zhipu", "ZhipuAI");
        vendorAlias.put("tongyi", "Tongyi");
        vendorAlias.put("qwen", "Tongyi");
        vendorAlias.put("aliyun", "Tongyi");
        vendorAlias.put("ali", "Tongyi");
        vendorAlias.put("baidu", "Baidu");
        vendorAlias.put("tencent", "Tencent");
    }

    public ModelProvider getProvider(String vendor) {
        String normalizedVendor = vendorAlias.getOrDefault(vendor.toLowerCase(), vendor);
        ModelProvider provider = providers.get(normalizedVendor);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown vendor: " + vendor);
        }
        return provider;
    }

    public boolean hasProvider(String vendor) {
        String normalizedVendor = vendorAlias.getOrDefault(vendor.toLowerCase(), vendor);
        return providers.containsKey(normalizedVendor);
    }
}
