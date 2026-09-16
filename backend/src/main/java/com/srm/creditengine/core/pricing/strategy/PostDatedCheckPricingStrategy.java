package com.srm.creditengine.core.pricing.strategy;

import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PostDatedCheckPricingStrategy extends CompoundDiscountPricingStrategy {

    private static final BigDecimal MONTHLY_SPREAD = new BigDecimal("0.025");

    @Override
    public ReceivableType supportedType() {
        return ReceivableType.POST_DATED_CHECK;
    }

    @Override
    public BigDecimal monthlySpread() {
        return MONTHLY_SPREAD;
    }
}
