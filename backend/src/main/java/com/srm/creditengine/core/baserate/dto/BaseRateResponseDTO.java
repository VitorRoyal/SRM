package com.srm.creditengine.core.baserate.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BaseRateResponseDTO(
        Long id,
        BigDecimal monthlyRate,
        Instant effectiveAt
) {
}
