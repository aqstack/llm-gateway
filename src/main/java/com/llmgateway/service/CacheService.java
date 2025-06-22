package com.llmgateway.service;

import com.llmgateway.model.dto.ChatRequest;
import com.llmgateway.model.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private static final String CACHE_PREFIX = "llm:cache:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${llm.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${llm.cache.ttl-minutes:60}")
    private int ttlMinutes;

    public Optional<ChatResponse> get(ChatRequest request) {
        if (!cacheEnabled) {
            return Optional.empty();
        }

        String key = buildKey(request);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof ChatResponse response) {
                log.debug("Cache hit for key: {}", key);
                return Optional.of(response);
            }
        } catch (Exception e) {
            log.warn("Cache read error: {}", e.getMessage());
        }

        return Optional.empty();
    }

    public void put(ChatRequest request, ChatResponse response) {
        if (!cacheEnabled) {
            return;
        }

        String key = buildKey(request);
        try {
            redisTemplate.opsForValue().set(key, response, Duration.ofMinutes(ttlMinutes));
            log.debug("Cached response for key: {}", key);
        } catch (Exception e) {
            log.warn("Cache write error: {}", e.getMessage());
        }
    }

    private String buildKey(ChatRequest request) {
        String content = request.provider() + ":" +
                request.model() + ":" +
                request.messages().toString() + ":" +
                request.temperature();

        return CACHE_PREFIX + hash(content);
    }

    private String hash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
