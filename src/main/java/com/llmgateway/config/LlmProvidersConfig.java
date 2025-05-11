package com.llmgateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "llm.providers")
@Data
public class LlmProvidersConfig {

    private ProviderConfig openai = new ProviderConfig();
    private ProviderConfig anthropic = new ProviderConfig();
    private ProviderConfig ollama = new ProviderConfig();

    @Data
    public static class ProviderConfig {
        private String apiKey;
        private String baseUrl;
        private boolean enabled = true;
        private int timeoutSeconds = 30;
        private int maxRetries = 3;
    }
}
