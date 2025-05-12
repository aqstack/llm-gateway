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
public class AnthropicProvider implements LlmProvider {

    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final WebClient webClient;

    public AnthropicProvider(
            WebClient.Builder webClientBuilder,
            @Value("${llm.providers.anthropic.api-key}") String apiKey,
            @Value("${llm.providers.anthropic.base-url}") String baseUrl) {

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", ANTHROPIC_VERSION)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String getName() {
        return "anthropic";
    }

    @Override
    public boolean supports(String providerName) {
        return "anthropic".equalsIgnoreCase(providerName);
    }

    @Override
    public Mono<ChatResponse> chat(ChatRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", request.model());
        body.put("max_tokens", request.maxTokens() != null ? request.maxTokens() : 4096);

        var messages = request.messages().stream()
                .filter(m -> !"system".equals(m.role()))
                .map(m -> Map.of("role", m.role(), "content", m.content()))
                .toList();
        body.put("messages", messages);

        request.messages().stream()
                .filter(m -> "system".equals(m.role()))
                .findFirst()
                .ifPresent(m -> body.put("system", m.content()));

        if (request.temperature() != null) {
            body.put("temperature", request.temperature());
        }

        return webClient.post()
                .uri("/messages")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseResponse);
    }

    private ChatResponse parseResponse(JsonNode json) {
        String id = json.path("id").asText(UUID.randomUUID().toString());

        StringBuilder content = new StringBuilder();
        for (JsonNode block : json.path("content")) {
            if ("text".equals(block.path("type").asText())) {
                content.append(block.path("text").asText());
            }
        }

        JsonNode usage = json.path("usage");
        int inputTokens = usage.path("input_tokens").asInt();
        int outputTokens = usage.path("output_tokens").asInt();

        return new ChatResponse(
                id,
                getName(),
                json.path("model").asText(),
                content.toString(),
                new ChatResponse.Usage(inputTokens, outputTokens, inputTokens + outputTokens),
                Instant.now()
        );
    }
}
