package com.srm.creditengine.core.pricing.dto;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;

import java.math.BigDecimal;

public record PricingInput(
        ReceivableType receivableType,
        BigDecimal faceValue,
        int termInMonths,
        BigDecimal monthlyBaseRate,
        CurrencyCode paymentCurrency,
        BigDecimal exchangeRate
) {
}
