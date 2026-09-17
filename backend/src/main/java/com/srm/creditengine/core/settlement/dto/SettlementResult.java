package com.srm.creditengine.core.settlement.dto;

public record SettlementResult(
        SettlementResponseDTO settlement,
        boolean replayed
) {
}
