package com.srm.creditengine.core.pricing.dto;

import com.srm.creditengine.core.baserate.entity.BaseRate;
import com.srm.creditengine.core.exchangerate.entity.ExchangeRate;

public record PricingQuote(
        int termInMonths,
        BaseRate baseRate,
        ExchangeRate exchangeRate,
        PricingResult pricingResult
) {
}
