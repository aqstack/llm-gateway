package com.llmgateway.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MetricsService {

    private final MeterRegistry registry;
    private final Map<String, Counter> requestCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> errorCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> latencyTimers = new ConcurrentHashMap<>();

    public MetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordRequest(String provider, String model) {
        String key = provider + ":" + model;
        requestCounters.computeIfAbsent(key, k ->
                Counter.builder("llm.requests")
                        .tag("provider", provider)
                        .tag("model", model)
                        .register(registry)
        ).increment();
    }

    public void recordError(String provider, String errorType) {
        String key = provider + ":" + errorType;
        errorCounters.computeIfAbsent(key, k ->
                Counter.builder("llm.errors")
                        .tag("provider", provider)
                        .tag("error_type", errorType)
                        .register(registry)
        ).increment();
    }

    public void recordLatency(String provider, Duration duration) {
        latencyTimers.computeIfAbsent(provider, k ->
                Timer.builder("llm.latency")
                        .tag("provider", provider)
                        .register(registry)
        ).record(duration);
    }

    public void recordTokens(String provider, int promptTokens, int completionTokens) {
        Counter.builder("llm.tokens.prompt")
                .tag("provider", provider)
                .register(registry)
                .increment(promptTokens);

        Counter.builder("llm.tokens.completion")
                .tag("provider", provider)
                .register(registry)
                .increment(completionTokens);
    }
}
