package com.llmgateway.service;

import com.llmgateway.model.ApiKey;
import com.llmgateway.model.UsageRecord;
import com.llmgateway.model.dto.ChatResponse;
import com.llmgateway.repository.UsageRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsageService {

    private final UsageRecordRepository usageRecordRepository;
    private final CostCalculator costCalculator;

    public void recordUsage(ApiKey apiKey, ChatResponse response, boolean cached) {
        BigDecimal cost = cached ? BigDecimal.ZERO :
                costCalculator.calculate(
                        response.model(),
                        response.usage().promptTokens(),
                        response.usage().completionTokens()
                );

        UsageRecord record = UsageRecord.builder()
                .apiKey(apiKey)
                .provider(response.provider())
                .model(response.model())
                .promptTokens(response.usage().promptTokens())
                .completionTokens(response.usage().completionTokens())
                .totalTokens(response.usage().totalTokens())
                .cost(cost)
                .cached(cached)
                .build();

        usageRecordRepository.save(record);
    }

    public UsageSummary getUsageSummary(Long apiKeyId) {
        BigDecimal totalCost = usageRecordRepository.getTotalCostByApiKeyId(apiKeyId);
        Long totalTokens = usageRecordRepository.getTotalTokensByApiKeyId(apiKeyId);

        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        BigDecimal last30DaysCost = usageRecordRepository.getCostSince(apiKeyId, thirtyDaysAgo);

        Map<String, BigDecimal> costByProvider = new HashMap<>();
        for (Object[] row : usageRecordRepository.getCostByProvider(apiKeyId)) {
            costByProvider.put((String) row[0], (BigDecimal) row[1]);
        }

        return new UsageSummary(
                totalCost != null ? totalCost : BigDecimal.ZERO,
                totalTokens != null ? totalTokens : 0L,
                last30DaysCost != null ? last30DaysCost : BigDecimal.ZERO,
                costByProvider
        );
    }

    public record UsageSummary(
            BigDecimal totalCost,
            long totalTokens,
            BigDecimal last30DaysCost,
            Map<String, BigDecimal> costByProvider
    ) {}
}
