package com.srm.creditengine.core.settlement.dto;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;

import java.time.Instant;

public record SettlementStatementFilter(
        Instant settledFrom,
        Instant settledUntil,
        Long assignorId,
        CurrencyCode paymentCurrency,
        int page,
        int size
) {
}
