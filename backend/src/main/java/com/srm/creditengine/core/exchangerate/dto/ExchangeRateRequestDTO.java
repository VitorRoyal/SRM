package com.srm.creditengine.core.exchangerate.dto;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeRateRequestDTO(
        @NotNull CurrencyCode currency,
        @NotNull @Positive @Digits(integer = 10, fraction = 8) BigDecimal brlPerUnit,
        Instant effectiveAt
) {
}
