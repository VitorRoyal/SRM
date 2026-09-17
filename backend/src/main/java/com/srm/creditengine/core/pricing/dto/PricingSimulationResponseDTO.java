package com.srm.creditengine.core.pricing.dto;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;

import java.math.BigDecimal;
import java.time.Instant;

public record PricingSimulationResponseDTO(
        int termInMonths,
        BigDecimal monthlyBaseRate,
        BigDecimal monthlySpread,
        BigDecimal presentValueBrl,
        BigDecimal discountBrl,
        CurrencyCode paymentCurrency,
        BigDecimal paymentAmount,
        BigDecimal exchangeRateBrlPerUnit,
        Instant exchangeRateEffectiveAt
) {
}
