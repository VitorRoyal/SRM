package com.srm.creditengine.core.settlement.dto;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record SettlementResponseDTO(
        Long id,
        Long assignorId,
        String assignorName,
        String documentNumber,
        ReceivableType receivableType,
        BigDecimal faceValue,
        LocalDate dueDate,
        int termInMonths,
        BigDecimal monthlyBaseRate,
        BigDecimal monthlySpread,
        BigDecimal presentValueBrl,
        BigDecimal discountBrl,
        CurrencyCode paymentCurrency,
        BigDecimal paymentAmount,
        BigDecimal exchangeRateBrlPerUnit,
        Instant settledAt
) {
}
