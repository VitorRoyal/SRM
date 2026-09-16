package com.srm.creditengine.core.exchangerate;

import com.srm.creditengine.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExchangeRateApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldReturnTheSeededRateAsCurrent() throws Exception {
        mockMvc.perform(get("/exchange-rates/current").param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.brlPerUnit").value(5.4321))
                .andExpect(jsonPath("$.effectiveAt").value("2020-01-01T00:00:00Z"));
    }

    @Test
    void shouldRegisterANewRateEffectiveImmediatelyWhenNoDateIsGiven() throws Exception {
        mockMvc.perform(registerRate("""
                        {"currency": "USD", "brlPerUnit": 5.10}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.brlPerUnit").value(5.10))
                .andExpect(jsonPath("$.effectiveAt").value("2026-09-14T15:00:00Z"));

        mockMvc.perform(get("/exchange-rates/current").param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.brlPerUnit").value(5.10));
    }

    @Test
    void shouldKeepTheCurrentRateUntilAFutureRateBecomesEffective() throws Exception {
        mockMvc.perform(registerRate("""
                        {"currency": "USD", "brlPerUnit": 6.00, "effectiveAt": "2026-09-15T00:00:00Z"}
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/exchange-rates/current").param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brlPerUnit").value(5.4321));
    }

    @Test
    void shouldRejectARateForTheReferenceCurrency() throws Exception {
        mockMvc.perform(registerRate("""
                        {"currency": "BRL", "brlPerUnit": 1}
                        """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("BRL is the reference currency and has no exchange rate"));

        assertEquals(1, countRows("exchange_rate"));
    }

    @Test
    void shouldReturnFieldErrorsForInvalidRates() throws Exception {
        mockMvc.perform(registerRate("""
                        {"brlPerUnit": -1}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.currency").exists())
                .andExpect(jsonPath("$.fieldErrors.brlPerUnit").exists());

        mockMvc.perform(registerRate("""
                        {"currency": "USD", "brlPerUnit": 5.123456789}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.brlPerUnit").exists());

        assertEquals(1, countRows("exchange_rate"));
    }

    @Test
    void shouldRejectUnknownCurrencies() throws Exception {
        mockMvc.perform(registerRate("""
                        {"currency": "EUR", "brlPerUnit": 6.2}
                        """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/exchange-rates/current").param("currency", "EUR"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenThereIsNoCurrentRate() throws Exception {
        jdbcTemplate.execute("TRUNCATE exchange_rate RESTART IDENTITY");

        mockMvc.perform(get("/exchange-rates/current").param("currency", "USD"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No current exchange rate for USD"));
    }

    private RequestBuilder registerRate(String body) {
        return post("/exchange-rates").contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
