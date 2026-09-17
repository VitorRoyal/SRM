package com.srm.creditengine.core.settlement;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;
import com.srm.creditengine.core.settlement.dto.SettlementRequestDTO;
import com.srm.creditengine.core.settlement.dto.SettlementResult;
import com.srm.creditengine.core.settlement.service.SettlementService;
import com.srm.creditengine.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Map;

import static com.srm.creditengine.support.TestcontainersConfiguration.FIXED_NOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementAuditIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SettlementService settlementService;

    @Test
    void shouldPersistTheRatesEffectivelyUsedInAForeignCurrencySettlement() {
        SettlementResult settlementResult = settlementService.settle(
                "audit-key-0001",
                tradeBillRequest("DUP-AUDIT-USD", CurrencyCode.USD)
        );

        Map<String, Object> storedSettlement = findStoredSettlement(settlementResult.settlement().id());

        assertEquals(3, storedSettlement.get("term_in_months"));
        assertEquals(1L, storedSettlement.get("base_rate_id"));
        assertEquals(new BigDecimal("0.010000"), storedSettlement.get("monthly_base_rate"));
        assertEquals(new BigDecimal("0.015000"), storedSettlement.get("monthly_spread"));
        assertEquals(new BigDecimal("92859.94"), storedSettlement.get("present_value_brl"));
        assertEquals(new BigDecimal("7140.06"), storedSettlement.get("discount_brl"));
        assertEquals("USD", storedSettlement.get("payment_currency"));
        assertEquals(new BigDecimal("17094.67"), storedSettlement.get("payment_amount"));
        assertEquals(1L, storedSettlement.get("exchange_rate_id"));
        assertEquals(new BigDecimal("5.43210000"), storedSettlement.get("exchange_rate_brl_per_unit"));
        assertEquals(FIXED_NOW, ((Timestamp) storedSettlement.get("settled_at")).toInstant());
    }

    @Test
    void shouldNotRecordAnExchangeRateForSettlementsInBrl() {
        SettlementResult settlementResult = settlementService.settle(
                "audit-key-0002",
                tradeBillRequest("DUP-AUDIT-BRL", CurrencyCode.BRL)
        );

        Map<String, Object> storedSettlement = findStoredSettlement(settlementResult.settlement().id());

        assertEquals(new BigDecimal("92859.94"), storedSettlement.get("payment_amount"));
        assertNull(storedSettlement.get("exchange_rate_id"));
        assertNull(storedSettlement.get("exchange_rate_brl_per_unit"));
    }

    @Test
    void shouldKeepTheRecordedExchangeRateWhenANewRateIsRegisteredLater() {
        SettlementResult settlementResult = settlementService.settle(
                "audit-key-0003",
                tradeBillRequest("DUP-AUDIT-LATER", CurrencyCode.USD)
        );

        jdbcTemplate.update(
                "INSERT INTO exchange_rate (currency, brl_per_unit, effective_at) VALUES ('USD', 6.00000000, ?)",
                Timestamp.from(FIXED_NOW)
        );

        assertEquals(
                new BigDecimal("5.43210000"),
                findStoredSettlement(settlementResult.settlement().id()).get("exchange_rate_brl_per_unit")
        );
    }

    @ParameterizedTest(name = "{0} is append-only")
    @CsvSource({
            "settlement, settled_at",
            "receivable, created_at"
    })
    void shouldRejectUpdatesAndDeletesAtDatabaseLevel(String tableName, String timestampColumn) {
        settlementService.settle("audit-key-0004", tradeBillRequest("DUP-AUDIT-LOCK", CurrencyCode.BRL));

        DataAccessException updateException = assertThrows(
                DataAccessException.class,
                () -> jdbcTemplate.update("UPDATE " + tableName + " SET " + timestampColumn + " = now()")
        );
        DataAccessException deleteException = assertThrows(
                DataAccessException.class,
                () -> jdbcTemplate.update("DELETE FROM " + tableName)
        );

        assertTrue(updateException.getMostSpecificCause().getMessage().contains("append-only"));
        assertTrue(deleteException.getMostSpecificCause().getMessage().contains("append-only"));
        assertEquals(1, countRows(tableName));
    }

    private Map<String, Object> findStoredSettlement(Long settlementId) {
        return jdbcTemplate.queryForMap(
                """
                SELECT term_in_months, base_rate_id, monthly_base_rate, monthly_spread, present_value_brl,
                       discount_brl, payment_currency, payment_amount, exchange_rate_id,
                       exchange_rate_brl_per_unit, settled_at
                FROM settlement WHERE id = ?
                """,
                settlementId
        );
    }

    private SettlementRequestDTO tradeBillRequest(String documentNumber, CurrencyCode paymentCurrency) {
        return new SettlementRequestDTO(
                1L,
                documentNumber,
                ReceivableType.TRADE_BILL,
                new BigDecimal("100000.00"),
                LocalDate.parse("2026-12-14"),
                paymentCurrency
        );
    }
}
