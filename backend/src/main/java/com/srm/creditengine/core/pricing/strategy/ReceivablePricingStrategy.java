package com.srm.creditengine.core.pricing.strategy;

import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;

import java.math.BigDecimal;

public interface ReceivablePricingStrategy {

    ReceivableType supportedType();

    BigDecimal monthlySpread();

    BigDecimal calculatePresentValue(BigDecimal faceValue, BigDecimal monthlyBaseRate, int termInMonths);
}
