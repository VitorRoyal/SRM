package com.srm.creditengine.core.pricing.mapper;

import com.srm.creditengine.core.pricing.dto.PricingQuote;
import com.srm.creditengine.core.pricing.dto.PricingResult;
import com.srm.creditengine.core.pricing.dto.PricingSimulationResponseDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PricingSimulationMapper {

    public PricingSimulationResponseDTO toResponseDTO(PricingQuote pricingQuote) {
        PricingResult pricingResult = pricingQuote.pricingResult();

        Instant exchangeRateEffectiveAt = null;
        if (pricingQuote.exchangeRate() != null) {
            exchangeRateEffectiveAt = pricingQuote.exchangeRate().getEffectiveAt();
        }

        return new PricingSimulationResponseDTO(
                pricingQuote.termInMonths(),
                pricingQuote.baseRate().getMonthlyRate(),
                pricingResult.monthlySpread(),
                pricingResult.presentValueBrl(),
                pricingResult.discountBrl(),
                pricingResult.paymentCurrency(),
                pricingResult.paymentAmount(),
                pricingResult.exchangeRate(),
                exchangeRateEffectiveAt
        );
    }
}
