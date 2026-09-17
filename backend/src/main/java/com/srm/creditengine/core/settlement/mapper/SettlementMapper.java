package com.srm.creditengine.core.settlement.mapper;

import com.srm.creditengine.core.receivable.entity.Receivable;
import com.srm.creditengine.core.settlement.dto.SettlementResponseDTO;
import com.srm.creditengine.core.settlement.entity.Settlement;
import org.springframework.stereotype.Component;

@Component
public class SettlementMapper {

    public SettlementResponseDTO toResponseDTO(Settlement settlement) {
        Receivable receivable = settlement.getReceivable();
        return new SettlementResponseDTO(
                settlement.getId(),
                receivable.getAssignor().getId(),
                receivable.getAssignor().getName(),
                receivable.getDocumentNumber(),
                receivable.getType(),
                receivable.getFaceValue(),
                receivable.getDueDate(),
                settlement.getTermInMonths(),
                settlement.getMonthlyBaseRate(),
                settlement.getMonthlySpread(),
                settlement.getPresentValueBrl(),
                settlement.getDiscountBrl(),
                settlement.getPaymentCurrency(),
                settlement.getPaymentAmount(),
                settlement.getExchangeRateBrlPerUnit(),
                settlement.getSettledAt()
        );
    }
}
