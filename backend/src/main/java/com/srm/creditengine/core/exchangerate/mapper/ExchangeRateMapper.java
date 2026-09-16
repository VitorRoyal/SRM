package com.srm.creditengine.core.exchangerate.mapper;

import com.srm.creditengine.core.exchangerate.dto.ExchangeRateResponseDTO;
import com.srm.creditengine.core.exchangerate.entity.ExchangeRate;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateMapper {

    public ExchangeRateResponseDTO toResponseDTO(ExchangeRate exchangeRate) {
        return new ExchangeRateResponseDTO(
                exchangeRate.getId(),
                exchangeRate.getCurrency(),
                exchangeRate.getBrlPerUnit(),
                exchangeRate.getEffectiveAt(),
                exchangeRate.getCreatedAt()
        );
    }
}
