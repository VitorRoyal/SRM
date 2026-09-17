package com.srm.creditengine.core.assignor.service;

import com.srm.creditengine.core.assignor.dto.AssignorResponseDTO;
import com.srm.creditengine.core.assignor.entity.Assignor;
import com.srm.creditengine.core.assignor.mapper.AssignorMapper;
import com.srm.creditengine.core.assignor.repository.AssignorRepository;
import com.srm.creditengine.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssignorService {

    private final AssignorRepository assignorRepository;
    private final AssignorMapper assignorMapper;

    public AssignorService(AssignorRepository assignorRepository, AssignorMapper assignorMapper) {
        this.assignorRepository = assignorRepository;
        this.assignorMapper = assignorMapper;
    }

    @Transactional(readOnly = true)
    public List<AssignorResponseDTO> getAll() {
        return assignorRepository.findAllByOrderByNameAsc()
                .stream()
                .map(assignorMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Assignor getById(Long id) {
        return assignorRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignor not found: " + id));
    }
}
