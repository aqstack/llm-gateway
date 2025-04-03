package com.llmgateway.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CostCalculatorTest {

    private final CostCalculator calculator = new CostCalculator();

    @Test
    void calculate_gpt4o_shouldReturnCorrectCost() {
        BigDecimal cost = calculator.calculate("gpt-4o", 1000, 500);

        // 1000 * 0.000005 + 500 * 0.000015 = 0.005 + 0.0075 = 0.0125
        assertThat(cost).isEqualByComparingTo(new BigDecimal("0.012500"));
    }

    @Test
    void calculate_gpt4oMini_shouldReturnCorrectCost() {
        BigDecimal cost = calculator.calculate("gpt-4o-mini", 1000, 500);

        // 1000 * 0.00000015 + 500 * 0.0000006 = 0.00015 + 0.0003 = 0.00045
        assertThat(cost).isEqualByComparingTo(new BigDecimal("0.000450"));
    }

    @Test
    void calculate_unknownModel_shouldUseDefaultPricing() {
        BigDecimal cost = calculator.calculate("unknown-model", 1000, 500);

        // 1000 * 0.000001 + 500 * 0.000002 = 0.001 + 0.001 = 0.002
        assertThat(cost).isEqualByComparingTo(new BigDecimal("0.002000"));
    }

    @Test
    void calculate_claude35Sonnet_shouldReturnCorrectCost() {
        BigDecimal cost = calculator.calculate("claude-3-5-sonnet-20241022", 1000, 500);

        // 1000 * 0.000003 + 500 * 0.000015 = 0.003 + 0.0075 = 0.0105
        assertThat(cost).isEqualByComparingTo(new BigDecimal("0.010500"));
    }
}
