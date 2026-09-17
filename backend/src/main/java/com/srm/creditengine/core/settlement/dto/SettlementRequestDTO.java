package com.srm.creditengine.core.settlement.dto;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SettlementRequestDTO(
        @NotNull Long assignorId,
        @NotBlank @Size(max = 50) String documentNumber,
        @NotNull ReceivableType receivableType,
        @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal faceValue,
        @NotNull LocalDate dueDate,
        @NotNull CurrencyCode paymentCurrency
) {
}
