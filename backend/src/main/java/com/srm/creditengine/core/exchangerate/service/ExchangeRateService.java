package com.srm.creditengine.core.exchangerate.service;

import com.srm.creditengine.core.exchangerate.dto.ExchangeRateRequestDTO;
import com.srm.creditengine.core.exchangerate.dto.ExchangeRateResponseDTO;
import com.srm.creditengine.core.exchangerate.entity.ExchangeRate;
import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.exchangerate.mapper.ExchangeRateMapper;
import com.srm.creditengine.core.exchangerate.repository.ExchangeRateRepository;
import com.srm.creditengine.shared.exception.BusinessRuleException;
import com.srm.creditengine.shared.exception.RateUnavailableException;
import com.srm.creditengine.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateMapper exchangeRateMapper;
    private final Clock clock;

    public ExchangeRateService(
            ExchangeRateRepository exchangeRateRepository,
            ExchangeRateMapper exchangeRateMapper,
            Clock clock
    ) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateMapper = exchangeRateMapper;
        this.clock = clock;
    }

    @Transactional
    public ExchangeRateResponseDTO register(ExchangeRateRequestDTO exchangeRateRequestDTO) {
        if (exchangeRateRequestDTO.currency() == CurrencyCode.BRL) {
            throw new BusinessRuleException("BRL is the reference currency and has no exchange rate");
        }

        Instant now = Instant.now(clock).truncatedTo(ChronoUnit.MICROS);
        Instant effectiveAt = now;
        if (exchangeRateRequestDTO.effectiveAt() != null) {
            effectiveAt = exchangeRateRequestDTO.effectiveAt().truncatedTo(ChronoUnit.MICROS);
        }

        ExchangeRate exchangeRate = exchangeRateRepository.save(new ExchangeRate(
                exchangeRateRequestDTO.currency(),
                exchangeRateRequestDTO.brlPerUnit(),
                effectiveAt,
                now
        ));
        return exchangeRateMapper.toResponseDTO(exchangeRate);
    }

    @Transactional(readOnly = true)
    public ExchangeRateResponseDTO getCurrent(CurrencyCode currency) {
        return findEffectiveAt(currency, Instant.now(clock))
                .map(exchangeRateMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No current exchange rate for " + currency));
    }

    @Transactional(readOnly = true)
    public ExchangeRate getEffectiveAt(CurrencyCode currency, Instant referenceInstant) {
        return findEffectiveAt(currency, referenceInstant)
                .orElseThrow(() -> new RateUnavailableException(
                        "No exchange rate for " + currency + " effective at " + referenceInstant));
    }

    private Optional<ExchangeRate> findEffectiveAt(CurrencyCode currency, Instant referenceInstant) {
        return exchangeRateRepository.findFirstByCurrencyAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(
                currency,
                referenceInstant
        );
    }
}
