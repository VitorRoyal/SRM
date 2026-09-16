package com.srm.creditengine.core.baserate.controller;

import com.srm.creditengine.core.baserate.dto.BaseRateResponseDTO;
import com.srm.creditengine.core.baserate.service.BaseRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/base-rates")
public class BaseRateController {

    private final BaseRateService baseRateService;

    public BaseRateController(BaseRateService baseRateService) {
        this.baseRateService = baseRateService;
    }

    @GetMapping("/current")
    public ResponseEntity<BaseRateResponseDTO> getCurrent() {
        return ResponseEntity.ok(baseRateService.getCurrent());
    }
}
