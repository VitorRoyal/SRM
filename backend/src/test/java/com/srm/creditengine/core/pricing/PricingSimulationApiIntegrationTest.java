package com.srm.creditengine.core.pricing;

import com.srm.creditengine.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PricingSimulationApiIntegrationTest extends AbstractIntegrationTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "C1, TRADE_BILL,       100000.00, 2026-12-14, BRL, 3, 92859.94, 7140.06, 92859.94",
            "C2, POST_DATED_CHECK, 25000.00,  2026-11-14, BRL, 2, 23337.77, 1662.23, 23337.77",
            "C3, TRADE_BILL,       100000.00, 2026-12-14, USD, 3, 92859.94, 7140.06, 17094.67"
    })
    void shouldReproduceGoldenCasesUsingRatesStoredInTheDatabase(
            String caseId,
            String receivableType,
            String faceValue,
            String dueDate,
            String paymentCurrency,
            int expectedTerm,
            double expectedPresentValue,
            double expectedDiscount,
            double expectedPaymentAmount
    ) throws Exception {
        mockMvc.perform(simulation("""
                        {"receivableType": "%s", "faceValue": %s, "dueDate": "%s", "paymentCurrency": "%s"}
                        """.formatted(receivableType, faceValue, dueDate, paymentCurrency)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.termInMonths").value(expectedTerm))
                .andExpect(jsonPath("$.monthlyBaseRate").value(0.01))
                .andExpect(jsonPath("$.presentValueBrl").value(expectedPresentValue))
                .andExpect(jsonPath("$.discountBrl").value(expectedDiscount))
                .andExpect(jsonPath("$.paymentCurrency").value(paymentCurrency))
                .andExpect(jsonPath("$.paymentAmount").value(expectedPaymentAmount));
    }

    @Test
    void shouldExposeTheRatesUsedInTheSimulation() throws Exception {
        mockMvc.perform(simulation("""
                        {"receivableType": "POST_DATED_CHECK", "faceValue": 1000.00, "dueDate": "2026-12-14", "paymentCurrency": "USD"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlySpread").value(0.025))
                .andExpect(jsonPath("$.exchangeRateBrlPerUnit").value(5.4321))
                .andExpect(jsonPath("$.exchangeRateEffectiveAt").value("2020-01-01T00:00:00Z"));

        mockMvc.perform(simulation("""
                        {"receivableType": "POST_DATED_CHECK", "faceValue": 1000.00, "dueDate": "2026-12-14", "paymentCurrency": "BRL"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exchangeRateBrlPerUnit").value(nullValue()))
                .andExpect(jsonPath("$.exchangeRateEffectiveAt").value(nullValue()));
    }

    @Test
    void shouldRoundAPartialMonthUpWhenCountingTheTerm() throws Exception {
        mockMvc.perform(simulation("""
                        {"receivableType": "TRADE_BILL", "faceValue": 1000.00, "dueDate": "2026-10-15", "paymentCurrency": "BRL"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.termInMonths").value(2));
    }

    @Test
    void shouldReturnFieldErrorsForInvalidInput() throws Exception {
        mockMvc.perform(simulation("""
                        {"receivableType": "TRADE_BILL", "faceValue": -5, "paymentCurrency": "BRL"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.faceValue").exists())
                .andExpect(jsonPath("$.fieldErrors.dueDate").exists());

        mockMvc.perform(simulation("""
                        {"receivableType": "TRADE_BILL", "faceValue": 10.001, "dueDate": "2026-12-14", "paymentCurrency": "BRL"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.faceValue").exists());
    }

    @Test
    void shouldRejectUnknownReceivableTypes() throws Exception {
        mockMvc.perform(simulation("""
                        {"receivableType": "BOND", "faceValue": 10, "dueDate": "2026-12-14", "paymentCurrency": "BRL"}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnprocessableContentForDueDateNotInTheFuture() throws Exception {
        mockMvc.perform(simulation("""
                        {"receivableType": "TRADE_BILL", "faceValue": 1000.00, "dueDate": "2026-09-14", "paymentCurrency": "BRL"}
                        """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Due date must be after 2026-09-14"));
    }

    @Test
    void shouldReturnUnprocessableContentWhenNoExchangeRateIsAvailable() throws Exception {
        jdbcTemplate.execute("TRUNCATE exchange_rate RESTART IDENTITY");

        mockMvc.perform(simulation("""
                        {"receivableType": "TRADE_BILL", "faceValue": 1000.00, "dueDate": "2026-12-14", "paymentCurrency": "USD"}
                        """))
                .andExpect(status().isUnprocessableContent());

        mockMvc.perform(simulation("""
                        {"receivableType": "TRADE_BILL", "faceValue": 1000.00, "dueDate": "2026-12-14", "paymentCurrency": "BRL"}
                        """))
                .andExpect(status().isOk());
    }

    private RequestBuilder simulation(String body) {
        return post("/pricing/simulations").contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
