package com.srm.creditengine.core.settlement.controller;

import com.srm.creditengine.core.settlement.dto.SettlementRequestDTO;
import com.srm.creditengine.core.settlement.dto.SettlementResponseDTO;
import com.srm.creditengine.core.settlement.dto.SettlementResult;
import com.srm.creditengine.core.settlement.service.SettlementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settlements")
public class SettlementController {

    private static final String IDEMPOTENT_REPLAYED_HEADER = "Idempotent-Replayed";

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping
    public ResponseEntity<SettlementResponseDTO> settle(
            @RequestHeader("Idempotency-Key") @Size(min = 8, max = 100) String idempotencyKey,
            @Valid @RequestBody SettlementRequestDTO settlementRequestDTO
    ) {
        SettlementResult settlementResult = settlementService.settle(idempotencyKey, settlementRequestDTO);
        if (settlementResult.replayed()) {
            return ResponseEntity.ok()
                    .header(IDEMPOTENT_REPLAYED_HEADER, "true")
                    .body(settlementResult.settlement());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(settlementResult.settlement());
    }
}
