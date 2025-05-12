package com.llmgateway.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ChatRequest(
    @NotBlank(message = "Provider is required")
    String provider,

    @NotBlank(message = "Model is required")
    String model,

    @NotEmpty(message = "Messages cannot be empty")
    List<Message> messages,

    Double temperature,
    Integer maxTokens,
    Boolean stream
) {
    public record Message(
        @NotBlank String role,
        @NotBlank String content
    ) {}
}
