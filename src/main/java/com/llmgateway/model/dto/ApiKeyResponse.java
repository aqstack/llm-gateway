package com.llmgateway.model.dto;

import com.llmgateway.model.ApiKey;

import java.time.Instant;

public record ApiKeyResponse(
    Long id,
    String name,
    String owner,
    boolean active,
    Instant createdAt,
    Instant lastUsedAt,
    int requestsPerMinute,
    String rawKey
) {
    public static ApiKeyResponse from(ApiKey apiKey) {
        return new ApiKeyResponse(
            apiKey.getId(),
            apiKey.getName(),
            apiKey.getOwner(),
            apiKey.isActive(),
            apiKey.getCreatedAt(),
            apiKey.getLastUsedAt(),
            apiKey.getRequestsPerMinute(),
            null
        );
    }

    public static ApiKeyResponse fromWithRawKey(ApiKey apiKey, String rawKey) {
        return new ApiKeyResponse(
            apiKey.getId(),
            apiKey.getName(),
            apiKey.getOwner(),
            apiKey.isActive(),
            apiKey.getCreatedAt(),
            apiKey.getLastUsedAt(),
            apiKey.getRequestsPerMinute(),
            rawKey
        );
    }
}
