package com.srm.creditengine.core.assignor.mapper;

import com.srm.creditengine.core.assignor.dto.AssignorResponseDTO;
import com.srm.creditengine.core.assignor.entity.Assignor;
import org.springframework.stereotype.Component;

@Component
public class AssignorMapper {

    public AssignorResponseDTO toResponseDTO(Assignor assignor) {
        return new AssignorResponseDTO(assignor.getId(), assignor.getName(), assignor.getTaxId());
    }
}
