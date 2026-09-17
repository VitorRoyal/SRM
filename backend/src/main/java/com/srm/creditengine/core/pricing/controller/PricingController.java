package com.srm.creditengine.core.pricing.controller;

import com.srm.creditengine.core.pricing.dto.PricingSimulationRequestDTO;
import com.srm.creditengine.core.pricing.dto.PricingSimulationResponseDTO;
import com.srm.creditengine.core.pricing.service.PricingQuoteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pricing")
public class PricingController {

    private final PricingQuoteService pricingQuoteService;

    public PricingController(PricingQuoteService pricingQuoteService) {
        this.pricingQuoteService = pricingQuoteService;
    }

    @PostMapping("/simulations")
    public ResponseEntity<PricingSimulationResponseDTO> simulate(
            @Valid @RequestBody PricingSimulationRequestDTO pricingSimulationRequestDTO
    ) {
        return ResponseEntity.ok(pricingQuoteService.simulate(pricingSimulationRequestDTO));
    }
}
