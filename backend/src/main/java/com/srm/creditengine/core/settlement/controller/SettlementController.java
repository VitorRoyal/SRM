package com.srm.creditengine.core.settlement.controller;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.settlement.dto.SettlementRequestDTO;
import com.srm.creditengine.core.settlement.dto.SettlementResponseDTO;
import com.srm.creditengine.core.settlement.dto.SettlementResult;
import com.srm.creditengine.core.settlement.service.SettlementService;
import com.srm.creditengine.core.settlement.service.SettlementStatementService;
import com.srm.creditengine.shared.dto.PageResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/settlements")
public class SettlementController {

    private static final String IDEMPOTENT_REPLAYED_HEADER = "Idempotent-Replayed";

    private final SettlementService settlementService;
    private final SettlementStatementService settlementStatementService;

    public SettlementController(
            SettlementService settlementService,
            SettlementStatementService settlementStatementService
    ) {
        this.settlementService = settlementService;
        this.settlementStatementService = settlementStatementService;
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

    @GetMapping
    public ResponseEntity<PageResponseDTO<SettlementResponseDTO>> getStatement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate settledFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate settledTo,
            @RequestParam(required = false) Long assignorId,
            @RequestParam(required = false) CurrencyCode paymentCurrency,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(settlementStatementService.getStatement(
                settledFrom,
                settledTo,
                assignorId,
                paymentCurrency,
                page,
                size
        ));
    }
}
