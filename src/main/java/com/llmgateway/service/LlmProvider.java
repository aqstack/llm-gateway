package com.llmgateway.service;

import com.llmgateway.model.dto.ChatRequest;
import com.llmgateway.model.dto.ChatResponse;
import reactor.core.publisher.Mono;

public interface LlmProvider {

    String getName();

    Mono<ChatResponse> chat(ChatRequest request);

    boolean supports(String providerName);
}
