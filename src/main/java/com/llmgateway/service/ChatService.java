package com.llmgateway.service;

import com.llmgateway.model.dto.ChatRequest;
import com.llmgateway.model.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final List<LlmProvider> providers;

    public Mono<ChatResponse> chat(ChatRequest request) {
        LlmProvider provider = providers.stream()
                .filter(p -> p.supports(request.provider()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported provider: " + request.provider()));

        return provider.chat(request);
    }
}
