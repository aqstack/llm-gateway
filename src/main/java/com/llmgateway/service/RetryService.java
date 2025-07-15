package com.llmgateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Supplier;

@Service
@Slf4j
public class RetryService {

    private static final int MAX_RETRIES = 3;
    private static final Duration INITIAL_BACKOFF = Duration.ofMillis(500);
    private static final Duration MAX_BACKOFF = Duration.ofSeconds(5);

    public <T> Mono<T> withRetry(Mono<T> operation, String operationName) {
        return operation.retryWhen(Retry.backoff(MAX_RETRIES, INITIAL_BACKOFF)
                .maxBackoff(MAX_BACKOFF)
                .filter(this::isRetryable)
                .doBeforeRetry(signal -> log.warn(
                        "Retrying {} after error: {} (attempt {})",
                        operationName,
                        signal.failure().getMessage(),
                        signal.totalRetries() + 1
                ))
                .onRetryExhaustedThrow((spec, signal) -> {
                    log.error("All retries exhausted for {}", operationName);
                    return signal.failure();
                }));
    }

    private boolean isRetryable(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null) return true;

        return message.contains("timeout") ||
               message.contains("connection") ||
               message.contains("503") ||
               message.contains("502") ||
               message.contains("429");
    }
}
