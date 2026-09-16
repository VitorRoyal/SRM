package com.srm.creditengine.core.pricing.dto;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;

import java.math.BigDecimal;

public record PricingResult(
        BigDecimal monthlySpread,
        BigDecimal presentValueBrl,
        BigDecimal discountBrl,
        CurrencyCode paymentCurrency,
        BigDecimal paymentAmount,
        BigDecimal exchangeRate
) {
}
