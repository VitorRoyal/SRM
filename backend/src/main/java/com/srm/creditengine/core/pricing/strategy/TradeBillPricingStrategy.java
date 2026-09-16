package com.srm.creditengine.core.pricing.strategy;

import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TradeBillPricingStrategy extends CompoundDiscountPricingStrategy {

    private static final BigDecimal MONTHLY_SPREAD = new BigDecimal("0.015");

    @Override
    public ReceivableType supportedType() {
        return ReceivableType.TRADE_BILL;
    }

    @Override
    public BigDecimal monthlySpread() {
        return MONTHLY_SPREAD;
    }
}
