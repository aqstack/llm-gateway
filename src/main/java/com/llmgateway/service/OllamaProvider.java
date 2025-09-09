package com.llmgateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.llmgateway.model.dto.ChatRequest;
import com.llmgateway.model.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OllamaProvider implements LlmProvider {

    private final WebClient webClient;

    public OllamaProvider(
            WebClient.Builder webClientBuilder,
            @Value("${llm.providers.ollama.base-url}") String baseUrl) {

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String getName() {
        return "ollama";
    }

    @Override
    public boolean supports(String providerName) {
        return "ollama".equalsIgnoreCase(providerName);
    }

    @Override
    public Mono<ChatResponse> chat(ChatRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", request.model());
        body.put("messages", request.messages().stream()
                .map(m -> Map.of("role", m.role(), "content", m.content()))
                .toList());
        body.put("stream", false);

        if (request.temperature() != null) {
            body.put("options", Map.of("temperature", request.temperature()));
        }

        return webClient.post()
                .uri("/api/chat")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseResponse);
    }

    private ChatResponse parseResponse(JsonNode json) {
        String content = json.path("message").path("content").asText();

        int promptTokens = json.path("prompt_eval_count").asInt(0);
        int completionTokens = json.path("eval_count").asInt(0);

        return new ChatResponse(
                UUID.randomUUID().toString(),
                getName(),
                json.path("model").asText(),
                content,
                new ChatResponse.Usage(promptTokens, completionTokens, promptTokens + completionTokens),
                Instant.now()
        );
    }
}
