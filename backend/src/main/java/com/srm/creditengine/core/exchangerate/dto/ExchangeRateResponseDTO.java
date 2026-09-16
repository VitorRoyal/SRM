package com.srm.creditengine.core.exchangerate.dto;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeRateResponseDTO(
        Long id,
        CurrencyCode currency,
        BigDecimal brlPerUnit,
        Instant effectiveAt,
        Instant createdAt
) {
}
