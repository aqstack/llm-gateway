package com.llmgateway.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
public class CostCalculator {

    private static final Map<String, TokenPricing> PRICING = Map.of(
            "gpt-4o", new TokenPricing(new BigDecimal("0.000005"), new BigDecimal("0.000015")),
            "gpt-4o-mini", new TokenPricing(new BigDecimal("0.00000015"), new BigDecimal("0.0000006")),
            "gpt-4-turbo", new TokenPricing(new BigDecimal("0.00001"), new BigDecimal("0.00003")),
            "gpt-3.5-turbo", new TokenPricing(new BigDecimal("0.0000005"), new BigDecimal("0.0000015")),
            "claude-3-5-sonnet-20241022", new TokenPricing(new BigDecimal("0.000003"), new BigDecimal("0.000015")),
            "claude-3-opus-20240229", new TokenPricing(new BigDecimal("0.000015"), new BigDecimal("0.000075")),
            "claude-3-haiku-20240307", new TokenPricing(new BigDecimal("0.00000025"), new BigDecimal("0.00000125"))
    );

    private static final TokenPricing DEFAULT_PRICING = new TokenPricing(
            new BigDecimal("0.000001"),
            new BigDecimal("0.000002")
    );

    public BigDecimal calculate(String model, int promptTokens, int completionTokens) {
        TokenPricing pricing = PRICING.getOrDefault(model, DEFAULT_PRICING);

        BigDecimal promptCost = pricing.inputPricePerToken()
                .multiply(BigDecimal.valueOf(promptTokens));

        BigDecimal completionCost = pricing.outputPricePerToken()
                .multiply(BigDecimal.valueOf(completionTokens));

        return promptCost.add(completionCost).setScale(6, RoundingMode.HALF_UP);
    }

    private record TokenPricing(BigDecimal inputPricePerToken, BigDecimal outputPricePerToken) {}
}
