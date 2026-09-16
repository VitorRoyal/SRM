package com.srm.creditengine.core.baserate.mapper;

import com.srm.creditengine.core.baserate.dto.BaseRateResponseDTO;
import com.srm.creditengine.core.baserate.entity.BaseRate;
import org.springframework.stereotype.Component;

@Component
public class BaseRateMapper {

    public BaseRateResponseDTO toResponseDTO(BaseRate baseRate) {
        return new BaseRateResponseDTO(baseRate.getId(), baseRate.getMonthlyRate(), baseRate.getEffectiveAt());
    }
}
