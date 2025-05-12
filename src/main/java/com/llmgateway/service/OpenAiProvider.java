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
public class OpenAiProvider implements LlmProvider {

    private final WebClient webClient;

    public OpenAiProvider(
            WebClient.Builder webClientBuilder,
            @Value("${llm.providers.openai.api-key}") String apiKey,
            @Value("${llm.providers.openai.base-url}") String baseUrl) {

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String getName() {
        return "openai";
    }

    @Override
    public boolean supports(String providerName) {
        return "openai".equalsIgnoreCase(providerName);
    }

    @Override
    public Mono<ChatResponse> chat(ChatRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", request.model());
        body.put("messages", request.messages().stream()
                .map(m -> Map.of("role", m.role(), "content", m.content()))
                .toList());

        if (request.temperature() != null) {
            body.put("temperature", request.temperature());
        }
        if (request.maxTokens() != null) {
            body.put("max_tokens", request.maxTokens());
        }

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseResponse);
    }

    private ChatResponse parseResponse(JsonNode json) {
        String id = json.path("id").asText(UUID.randomUUID().toString());
        String content = json.path("choices").get(0).path("message").path("content").asText();

        JsonNode usage = json.path("usage");
        int promptTokens = usage.path("prompt_tokens").asInt();
        int completionTokens = usage.path("completion_tokens").asInt();

        return new ChatResponse(
                id,
                getName(),
                json.path("model").asText(),
                content,
                new ChatResponse.Usage(promptTokens, completionTokens, promptTokens + completionTokens),
                Instant.now()
        );
    }
}
