package com.srm.creditengine.core.pricing.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class MonetaryPolicy {

    public static final MathContext CALCULATION_CONTEXT = MathContext.DECIMAL128;
    public static final int MONEY_SCALE = 2;
    public static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_EVEN;

    private MonetaryPolicy() {
    }

    public static BigDecimal roundMoney(BigDecimal value) {
        return value.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }
}
