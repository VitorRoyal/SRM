package com.srm.creditengine.core.settlement.service;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.settlement.dto.SettlementResponseDTO;
import com.srm.creditengine.core.settlement.dto.SettlementStatementFilter;
import com.srm.creditengine.core.settlement.repository.SettlementStatementRepository;
import com.srm.creditengine.shared.dto.PageResponseDTO;
import com.srm.creditengine.shared.exception.InvalidRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class SettlementStatementService {

    private final SettlementStatementRepository settlementStatementRepository;
    private final Clock clock;

    public SettlementStatementService(SettlementStatementRepository settlementStatementRepository, Clock clock) {
        this.settlementStatementRepository = settlementStatementRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<SettlementResponseDTO> getStatement(
            LocalDate settledFrom,
            LocalDate settledTo,
            Long assignorId,
            CurrencyCode paymentCurrency,
            int page,
            int size
    ) {
        if (settledFrom != null && settledTo != null && settledFrom.isAfter(settledTo)) {
            throw new InvalidRequestException("settledFrom must not be after settledTo");
        }

        SettlementStatementFilter settlementStatementFilter = new SettlementStatementFilter(
                startOfDay(settledFrom),
                startOfNextDay(settledTo),
                assignorId,
                paymentCurrency,
                page,
                size
        );

        long totalElements = settlementStatementRepository.count(settlementStatementFilter);
        List<SettlementResponseDTO> content = List.of();
        if (totalElements > (long) page * size) {
            content = settlementStatementRepository.findPage(settlementStatementFilter);
        }
        return PageResponseDTO.of(content, page, size, totalElements);
    }

    private Instant startOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay(clock.getZone()).toInstant();
    }

    private Instant startOfNextDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
    }
}
