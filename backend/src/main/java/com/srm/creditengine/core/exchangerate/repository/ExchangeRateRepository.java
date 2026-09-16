package com.srm.creditengine.core.exchangerate.repository;

import com.srm.creditengine.core.exchangerate.entity.ExchangeRate;
import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findFirstByCurrencyAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(
            CurrencyCode currency,
            Instant referenceInstant
    );
}
