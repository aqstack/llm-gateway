package com.llmgateway.service;

import com.llmgateway.model.dto.ProviderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderHealthService {

    private final List<LlmProvider> providers;
    private final Map<String, ProviderStatus> statusCache = new ConcurrentHashMap<>();

    private static final Map<String, List<String>> SUPPORTED_MODELS = Map.of(
            "openai", List.of("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo"),
            "anthropic", List.of("claude-3-5-sonnet-20241022", "claude-3-opus-20240229", "claude-3-haiku-20240307"),
            "ollama", List.of("llama2", "mistral", "codellama", "phi")
    );

    public List<ProviderStatus> getAllStatus() {
        return providers.stream()
                .map(this::getProviderStatus)
                .toList();
    }

    public ProviderStatus getProviderStatus(LlmProvider provider) {
        String name = provider.getName();
        List<String> models = SUPPORTED_MODELS.getOrDefault(name, List.of());
        return ProviderStatus.available(name, models);
    }

    public ProviderStatus getStatus(String providerName) {
        return providers.stream()
                .filter(p -> p.supports(providerName))
                .findFirst()
                .map(this::getProviderStatus)
                .orElse(ProviderStatus.unavailable(providerName, "Provider not configured"));
    }
}
