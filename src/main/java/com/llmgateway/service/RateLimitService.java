package com.llmgateway.service;

import com.llmgateway.model.ApiKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private static final String RATE_LIMIT_PREFIX = "llm:ratelimit:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${llm.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    public boolean isAllowed(ApiKey apiKey) {
        if (!rateLimitEnabled) {
            return true;
        }

        String key = RATE_LIMIT_PREFIX + apiKey.getId();
        int limit = apiKey.getRequestsPerMinute();

        try {
            Long currentCount = redisTemplate.opsForValue().increment(key);

            if (currentCount == null) {
                return true;
            }

            if (currentCount == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }

            boolean allowed = currentCount <= limit;

            if (!allowed) {
                log.warn("Rate limit exceeded for API key: {} (count: {}, limit: {})",
                        apiKey.getId(), currentCount, limit);
            }

            return allowed;

        } catch (Exception e) {
            log.warn("Rate limit check error: {}", e.getMessage());
            return true;
        }
    }

    public RateLimitInfo getRateLimitInfo(ApiKey apiKey) {
        String key = RATE_LIMIT_PREFIX + apiKey.getId();
        int limit = apiKey.getRequestsPerMinute();

        try {
            Object value = redisTemplate.opsForValue().get(key);
            int used = value != null ? ((Number) value).intValue() : 0;
            Long ttl = redisTemplate.getExpire(key);

            return new RateLimitInfo(limit, limit - used, ttl != null ? ttl : 60);
        } catch (Exception e) {
            return new RateLimitInfo(limit, limit, 60);
        }
    }

    public record RateLimitInfo(int limit, int remaining, long resetInSeconds) {}
}
