package com.srm.creditengine.core.baserate;

import com.srm.creditengine.core.baserate.service.BaseRateService;
import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.exchangerate.service.ExchangeRateService;
import com.srm.creditengine.shared.exception.RateUnavailableException;
import com.srm.creditengine.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReferenceRateIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BaseRateService baseRateService;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Test
    void shouldReturnTheCurrentBaseRateAsAFraction() throws Exception {
        mockMvc.perform(get("/base-rates/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyRate").value(0.01))
                .andExpect(jsonPath("$.effectiveAt").value("2020-01-01T00:00:00Z"));
    }

    @Test
    void shouldPickTheLatestRateEffectiveAtTheReferenceInstant() {
        jdbcTemplate.update(
                "INSERT INTO base_rate (monthly_rate, effective_at) VALUES (0.012000, '2026-01-01T00:00:00Z')"
        );

        assertEquals(
                new BigDecimal("0.010000"),
                baseRateService.getEffectiveAt(Instant.parse("2025-12-31T23:59:59Z")).getMonthlyRate()
        );
        assertEquals(
                new BigDecimal("0.012000"),
                baseRateService.getEffectiveAt(Instant.parse("2026-01-01T00:00:00Z")).getMonthlyRate()
        );
    }

    @Test
    void shouldFailWithBusinessErrorWhenNoRateIsEffectiveYet() {
        Instant beforeAnyRate = Instant.parse("2019-12-31T23:59:59Z");

        assertThrows(RateUnavailableException.class, () -> baseRateService.getEffectiveAt(beforeAnyRate));
        assertThrows(
                RateUnavailableException.class,
                () -> exchangeRateService.getEffectiveAt(CurrencyCode.USD, beforeAnyRate)
        );
    }

    @ParameterizedTest(name = "{0} is append-only")
    @ValueSource(strings = {"base_rate", "exchange_rate"})
    void shouldRejectUpdatesAndDeletesAtDatabaseLevel(String tableName) {
        DataAccessException updateException = assertThrows(
                DataAccessException.class,
                () -> jdbcTemplate.update("UPDATE " + tableName + " SET effective_at = now()")
        );
        DataAccessException deleteException = assertThrows(
                DataAccessException.class,
                () -> jdbcTemplate.update("DELETE FROM " + tableName)
        );

        assertTrue(updateException.getMostSpecificCause().getMessage().contains("append-only"));
        assertTrue(deleteException.getMostSpecificCause().getMessage().contains("append-only"));
        assertEquals(1, countRows(tableName));
    }
}
