package com.llmgateway.model.dto;

import java.time.Instant;
import java.util.List;

public record ProviderStatus(
    String provider,
    boolean available,
    List<String> supportedModels,
    Instant lastChecked,
    String errorMessage
) {
    public static ProviderStatus available(String provider, List<String> models) {
        return new ProviderStatus(provider, true, models, Instant.now(), null);
    }

    public static ProviderStatus unavailable(String provider, String error) {
        return new ProviderStatus(provider, false, List.of(), Instant.now(), error);
    }
}
