package com.srm.creditengine.core.baserate.repository;

import com.srm.creditengine.core.baserate.entity.BaseRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface BaseRateRepository extends JpaRepository<BaseRate, Long> {

    Optional<BaseRate> findFirstByEffectiveAtLessThanEqualOrderByEffectiveAtDesc(Instant referenceInstant);
}
