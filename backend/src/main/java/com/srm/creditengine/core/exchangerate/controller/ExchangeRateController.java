package com.srm.creditengine.core.exchangerate.controller;

import com.srm.creditengine.core.exchangerate.dto.ExchangeRateRequestDTO;
import com.srm.creditengine.core.exchangerate.dto.ExchangeRateResponseDTO;
import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.exchangerate.service.ExchangeRateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @PostMapping
    public ResponseEntity<ExchangeRateResponseDTO> register(
            @Valid @RequestBody ExchangeRateRequestDTO exchangeRateRequestDTO
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exchangeRateService.register(exchangeRateRequestDTO));
    }

    @GetMapping("/current")
    public ResponseEntity<ExchangeRateResponseDTO> getCurrent(@RequestParam CurrencyCode currency) {
        return ResponseEntity.ok(exchangeRateService.getCurrent(currency));
    }
}
