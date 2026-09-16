package com.srm.creditengine.core.pricing.strategy;

import java.math.BigDecimal;

import static com.srm.creditengine.core.pricing.service.MonetaryPolicy.CALCULATION_CONTEXT;

public abstract class CompoundDiscountPricingStrategy implements ReceivablePricingStrategy {

    @Override
    public BigDecimal calculatePresentValue(BigDecimal faceValue, BigDecimal monthlyBaseRate, int termInMonths) {
        BigDecimal monthlyDiscountRate = monthlyBaseRate.add(monthlySpread());
        BigDecimal discountFactor = BigDecimal.ONE.add(monthlyDiscountRate).pow(termInMonths, CALCULATION_CONTEXT);
        return faceValue.divide(discountFactor, CALCULATION_CONTEXT);
    }
}
