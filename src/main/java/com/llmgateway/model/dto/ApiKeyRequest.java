package com.llmgateway.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ApiKeyRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Owner is required")
    String owner,

    @Positive(message = "Requests per minute must be positive")
    Integer requestsPerMinute
) {}
