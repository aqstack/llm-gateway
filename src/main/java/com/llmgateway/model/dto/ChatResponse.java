package com.llmgateway.model.dto;

import java.time.Instant;

public record ChatResponse(
    String id,
    String provider,
    String model,
    String content,
    Usage usage,
    Instant timestamp
) {
    public record Usage(
        int promptTokens,
        int completionTokens,
        int totalTokens
    ) {}
}
