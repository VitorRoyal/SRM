package com.srm.creditengine.core.settlement;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;
import com.srm.creditengine.core.settlement.dto.SettlementRequestDTO;
import com.srm.creditengine.core.settlement.service.SettlementService;
import com.srm.creditengine.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SettlementStatementIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SettlementService settlementService;

    @BeforeEach
    void createSettlements() {
        settle(1L, "DOC-1", CurrencyCode.BRL);
        settle(1L, "DOC-2", CurrencyCode.USD);
        settle(2L, "DOC-3", CurrencyCode.BRL);
        settle(2L, "DOC-4", CurrencyCode.USD);
        settle(3L, "DOC-5", CurrencyCode.BRL);
    }

    @Test
    void shouldReturnTheFirstPageWithDefaultSize() throws Exception {
        mockMvc.perform(get("/settlements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content", hasSize(5)));
    }

    @Test
    void shouldPaginateOnTheServerOrderedFromNewestToOldest() throws Exception {
        mockMvc.perform(get("/settlements").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content[*].documentNumber", contains("DOC-5", "DOC-4")));

        mockMvc.perform(get("/settlements").param("page", "2").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].documentNumber", contains("DOC-1")));

        mockMvc.perform(get("/settlements").param("page", "3").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void shouldExposeTheAuditedValuesOfEachSettlement() throws Exception {
        mockMvc.perform(get("/settlements").param("assignorId", "2").param("paymentCurrency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].documentNumber").value("DOC-4"))
                .andExpect(jsonPath("$.content[0].assignorName").value("Beta Distribuidora de Alimentos S.A."))
                .andExpect(jsonPath("$.content[0].receivableType").value("POST_DATED_CHECK"))
                .andExpect(jsonPath("$.content[0].termInMonths").value(2))
                .andExpect(jsonPath("$.content[0].presentValueBrl").value(23337.77))
                .andExpect(jsonPath("$.content[0].paymentAmount").value(4296.27))
                .andExpect(jsonPath("$.content[0].exchangeRateBrlPerUnit").value(5.4321))
                .andExpect(jsonPath("$.content[0].settledAt").value("2026-09-14T15:00:00Z"));
    }

    @Test
    void shouldFilterByCurrency() throws Exception {
        mockMvc.perform(get("/settlements").param("paymentCurrency", "BRL"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[*].paymentCurrency", everyItem(is("BRL"))))
                .andExpect(jsonPath("$.content[*].exchangeRateBrlPerUnit", everyItem(nullValue())));
    }

    @Test
    void shouldFilterByAssignor() throws Exception {
        mockMvc.perform(get("/settlements").param("assignorId", "1"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].documentNumber", contains("DOC-2", "DOC-1")));

        mockMvc.perform(get("/settlements").param("assignorId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldFilterByPeriodUsingTheBusinessCalendarDay() throws Exception {
        mockMvc.perform(get("/settlements").param("settledFrom", "2026-09-14").param("settledTo", "2026-09-14"))
                .andExpect(jsonPath("$.totalElements").value(5));

        mockMvc.perform(get("/settlements").param("settledTo", "2026-09-13"))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        mockMvc.perform(get("/settlements").param("settledFrom", "2026-09-15"))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldRejectInvalidStatementParameters() throws Exception {
        mockMvc.perform(get("/settlements").param("settledFrom", "2026-09-15").param("settledTo", "2026-09-14"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("settledFrom must not be after settledTo"));

        mockMvc.perform(get("/settlements").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.size").exists());

        mockMvc.perform(get("/settlements").param("page", "-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/settlements").param("paymentCurrency", "EUR"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/settlements").param("settledFrom", "14/09/2026"))
                .andExpect(status().isBadRequest());
    }

    private void settle(Long assignorId, String documentNumber, CurrencyCode paymentCurrency) {
        settlementService.settle("statement-" + documentNumber, new SettlementRequestDTO(
                assignorId,
                documentNumber,
                ReceivableType.POST_DATED_CHECK,
                new BigDecimal("25000.00"),
                LocalDate.parse("2026-11-14"),
                paymentCurrency
        ));
    }
}
