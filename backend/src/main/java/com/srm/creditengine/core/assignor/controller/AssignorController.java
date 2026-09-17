package com.srm.creditengine.core.assignor.controller;

import com.srm.creditengine.core.assignor.dto.AssignorResponseDTO;
import com.srm.creditengine.core.assignor.service.AssignorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/assignors")
public class AssignorController {

    private final AssignorService assignorService;

    public AssignorController(AssignorService assignorService) {
        this.assignorService = assignorService;
    }

    @GetMapping
    public ResponseEntity<List<AssignorResponseDTO>> getAll() {
        return ResponseEntity.ok(assignorService.getAll());
    }
}
