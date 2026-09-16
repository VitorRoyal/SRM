package com.srm.creditengine.core.baserate.service;

import com.srm.creditengine.core.baserate.dto.BaseRateResponseDTO;
import com.srm.creditengine.core.baserate.entity.BaseRate;
import com.srm.creditengine.core.baserate.mapper.BaseRateMapper;
import com.srm.creditengine.core.baserate.repository.BaseRateRepository;
import com.srm.creditengine.shared.exception.RateUnavailableException;
import com.srm.creditengine.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
public class BaseRateService {

    private final BaseRateRepository baseRateRepository;
    private final BaseRateMapper baseRateMapper;
    private final Clock clock;

    public BaseRateService(BaseRateRepository baseRateRepository, BaseRateMapper baseRateMapper, Clock clock) {
        this.baseRateRepository = baseRateRepository;
        this.baseRateMapper = baseRateMapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public BaseRateResponseDTO getCurrent() {
        return findEffectiveAt(Instant.now(clock))
                .map(baseRateMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No current base rate"));
    }

    @Transactional(readOnly = true)
    public BaseRate getEffectiveAt(Instant referenceInstant) {
        return findEffectiveAt(referenceInstant)
                .orElseThrow(() -> new RateUnavailableException("No base rate effective at " + referenceInstant));
    }

    private Optional<BaseRate> findEffectiveAt(Instant referenceInstant) {
        return baseRateRepository.findFirstByEffectiveAtLessThanEqualOrderByEffectiveAtDesc(referenceInstant);
    }
}
