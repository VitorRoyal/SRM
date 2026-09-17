package com.srm.creditengine.core.pricing.dto;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PricingSimulationRequestDTO(
        @NotNull ReceivableType receivableType,
        @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal faceValue,
        @NotNull LocalDate dueDate,
        @NotNull CurrencyCode paymentCurrency
) {
}
