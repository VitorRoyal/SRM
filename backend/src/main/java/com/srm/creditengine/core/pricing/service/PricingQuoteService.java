package com.srm.creditengine.core.pricing.service;

import com.srm.creditengine.core.baserate.entity.BaseRate;
import com.srm.creditengine.core.baserate.service.BaseRateService;
import com.srm.creditengine.core.exchangerate.entity.ExchangeRate;
import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.exchangerate.service.ExchangeRateService;
import com.srm.creditengine.core.pricing.dto.PricingInput;
import com.srm.creditengine.core.pricing.dto.PricingQuote;
import com.srm.creditengine.core.pricing.dto.PricingResult;
import com.srm.creditengine.core.pricing.dto.PricingSimulationRequestDTO;
import com.srm.creditengine.core.pricing.dto.PricingSimulationResponseDTO;
import com.srm.creditengine.core.pricing.mapper.PricingSimulationMapper;
import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

@Service
public class PricingQuoteService {

    private final PricingEngine pricingEngine;
    private final BaseRateService baseRateService;
    private final ExchangeRateService exchangeRateService;
    private final PricingSimulationMapper pricingSimulationMapper;
    private final Clock clock;

    public PricingQuoteService(
            PricingEngine pricingEngine,
            BaseRateService baseRateService,
            ExchangeRateService exchangeRateService,
            PricingSimulationMapper pricingSimulationMapper,
            Clock clock
    ) {
        this.pricingEngine = pricingEngine;
        this.baseRateService = baseRateService;
        this.exchangeRateService = exchangeRateService;
        this.pricingSimulationMapper = pricingSimulationMapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PricingSimulationResponseDTO simulate(PricingSimulationRequestDTO pricingSimulationRequestDTO) {
        PricingQuote pricingQuote = quote(
                pricingSimulationRequestDTO.receivableType(),
                pricingSimulationRequestDTO.faceValue(),
                pricingSimulationRequestDTO.dueDate(),
                pricingSimulationRequestDTO.paymentCurrency(),
                Instant.now(clock)
        );
        return pricingSimulationMapper.toResponseDTO(pricingQuote);
    }

    @Transactional(readOnly = true)
    public PricingQuote quote(
            ReceivableType receivableType,
            BigDecimal faceValue,
            LocalDate dueDate,
            CurrencyCode paymentCurrency,
            Instant referenceInstant
    ) {
        LocalDate referenceDate = LocalDate.ofInstant(referenceInstant, clock.getZone());
        int termInMonths = TermCalculator.monthsUntil(referenceDate, dueDate);
        BaseRate baseRate = baseRateService.getEffectiveAt(referenceInstant);
        ExchangeRate exchangeRate = resolveExchangeRate(paymentCurrency, referenceInstant);

        BigDecimal exchangeRateBrlPerUnit = null;
        if (exchangeRate != null) {
            exchangeRateBrlPerUnit = exchangeRate.getBrlPerUnit();
        }

        PricingResult pricingResult = pricingEngine.price(new PricingInput(
                receivableType,
                faceValue,
                termInMonths,
                baseRate.getMonthlyRate(),
                paymentCurrency,
                exchangeRateBrlPerUnit
        ));

        return new PricingQuote(termInMonths, baseRate, exchangeRate, pricingResult);
    }

    private ExchangeRate resolveExchangeRate(CurrencyCode paymentCurrency, Instant referenceInstant) {
        if (paymentCurrency == null || paymentCurrency == CurrencyCode.BRL) {
            return null;
        }
        return exchangeRateService.getEffectiveAt(paymentCurrency, referenceInstant);
    }
}
