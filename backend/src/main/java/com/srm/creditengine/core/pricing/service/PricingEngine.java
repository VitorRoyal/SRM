package com.srm.creditengine.core.pricing.service;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.pricing.dto.PricingInput;
import com.srm.creditengine.core.pricing.dto.PricingResult;
import com.srm.creditengine.core.pricing.exception.InvalidPricingInputException;
import com.srm.creditengine.core.pricing.strategy.ReceivablePricingStrategy;
import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.srm.creditengine.core.pricing.service.MonetaryPolicy.CALCULATION_CONTEXT;
import static com.srm.creditengine.core.pricing.service.MonetaryPolicy.roundMoney;

@Service
public class PricingEngine {

    private final Map<ReceivableType, ReceivablePricingStrategy> pricingStrategiesByType;

    public PricingEngine(List<ReceivablePricingStrategy> receivablePricingStrategies) {
        this.pricingStrategiesByType = new EnumMap<>(ReceivableType.class);
        for (ReceivablePricingStrategy receivablePricingStrategy : receivablePricingStrategies) {
            ReceivablePricingStrategy previousStrategy =
                    pricingStrategiesByType.put(receivablePricingStrategy.supportedType(), receivablePricingStrategy);
            if (previousStrategy != null) {
                throw new IllegalStateException(
                        "More than one pricing strategy registered for " + receivablePricingStrategy.supportedType());
            }
        }
    }

    public PricingResult price(PricingInput pricingInput) {
        validate(pricingInput);

        ReceivablePricingStrategy receivablePricingStrategy = getStrategy(pricingInput.receivableType());
        BigDecimal presentValueBrl = roundMoney(receivablePricingStrategy.calculatePresentValue(
                pricingInput.faceValue(),
                pricingInput.monthlyBaseRate(),
                pricingInput.termInMonths()
        ));
        BigDecimal discountBrl = pricingInput.faceValue().subtract(presentValueBrl);

        if (pricingInput.paymentCurrency() == CurrencyCode.BRL) {
            return new PricingResult(
                    receivablePricingStrategy.monthlySpread(),
                    presentValueBrl,
                    discountBrl,
                    CurrencyCode.BRL,
                    presentValueBrl,
                    null
            );
        }

        BigDecimal paymentAmount = roundMoney(presentValueBrl.divide(pricingInput.exchangeRate(), CALCULATION_CONTEXT));
        return new PricingResult(
                receivablePricingStrategy.monthlySpread(),
                presentValueBrl,
                discountBrl,
                pricingInput.paymentCurrency(),
                paymentAmount,
                pricingInput.exchangeRate()
        );
    }

    private ReceivablePricingStrategy getStrategy(ReceivableType receivableType) {
        ReceivablePricingStrategy receivablePricingStrategy = pricingStrategiesByType.get(receivableType);
        if (receivablePricingStrategy == null) {
            throw new InvalidPricingInputException("No pricing strategy for receivable type " + receivableType);
        }
        return receivablePricingStrategy;
    }

    private void validate(PricingInput pricingInput) {
        if (pricingInput.receivableType() == null) {
            throw new InvalidPricingInputException("Receivable type is required");
        }
        if (pricingInput.faceValue() == null || pricingInput.faceValue().signum() <= 0) {
            throw new InvalidPricingInputException("Face value must be greater than zero");
        }
        if (pricingInput.termInMonths() < 1) {
            throw new InvalidPricingInputException("Term must be at least one month");
        }
        if (pricingInput.monthlyBaseRate() == null || pricingInput.monthlyBaseRate().signum() < 0) {
            throw new InvalidPricingInputException("Monthly base rate must be zero or positive");
        }
        if (pricingInput.paymentCurrency() == null) {
            throw new InvalidPricingInputException("Payment currency is required");
        }
        if (pricingInput.paymentCurrency() != CurrencyCode.BRL) {
            if (pricingInput.exchangeRate() == null || pricingInput.exchangeRate().signum() <= 0) {
                throw new InvalidPricingInputException(
                        "A positive exchange rate is required for payment in " + pricingInput.paymentCurrency());
            }
        }
    }
}
