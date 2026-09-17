package com.srm.creditengine.core.settlement;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;
import com.srm.creditengine.core.settlement.dto.SettlementRequestDTO;
import com.srm.creditengine.core.settlement.dto.SettlementResult;
import com.srm.creditengine.core.settlement.service.SettlementService;
import com.srm.creditengine.shared.exception.ConflictException;
import com.srm.creditengine.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SettlementIdempotencyIntegrationTest extends AbstractIntegrationTest {

    private static final int CONCURRENT_REQUESTS = 8;

    private static final String TRADE_BILL_IN_USD = """
            {
              "assignorId": 1,
              "documentNumber": "DUP-0001",
              "receivableType": "TRADE_BILL",
              "faceValue": 100000.00,
              "dueDate": "2026-12-14",
              "paymentCurrency": "USD"
            }
            """;

    @Autowired
    private SettlementService settlementService;

    @Test
    void shouldCreateOnceAndReplayTheSameSettlementOnRetry() throws Exception {
        mockMvc.perform(settlementRequest("retry-key-0001", TRADE_BILL_IN_USD))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Idempotent-Replayed"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.assignorName").value("Alfa Comercio de Tecidos Ltda"))
                .andExpect(jsonPath("$.termInMonths").value(3))
                .andExpect(jsonPath("$.presentValueBrl").value(92859.94))
                .andExpect(jsonPath("$.paymentAmount").value(17094.67))
                .andExpect(jsonPath("$.settledAt").value("2026-09-14T15:00:00Z"));

        mockMvc.perform(settlementRequest("retry-key-0001", TRADE_BILL_IN_USD))
                .andExpect(status().isOk())
                .andExpect(header().string("Idempotent-Replayed", "true"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paymentAmount").value(17094.67))
                .andExpect(jsonPath("$.settledAt").value("2026-09-14T15:00:00Z"));

        assertEquals(1, countRows("settlement"));
        assertEquals(1, countRows("receivable"));
    }

    @Test
    void shouldTreatEquivalentFaceValuesAsTheSameRequest() throws Exception {
        mockMvc.perform(settlementRequest("scale-key-0001", TRADE_BILL_IN_USD))
                .andExpect(status().isCreated());

        mockMvc.perform(settlementRequest("scale-key-0001", TRADE_BILL_IN_USD.replace("100000.00", "100000")))
                .andExpect(status().isOk())
                .andExpect(header().string("Idempotent-Replayed", "true"));
    }

    @Test
    void shouldRejectTheSameKeyWithADifferentPayload() throws Exception {
        mockMvc.perform(settlementRequest("reused-key-0001", TRADE_BILL_IN_USD))
                .andExpect(status().isCreated());

        mockMvc.perform(settlementRequest("reused-key-0001", TRADE_BILL_IN_USD.replace("100000.00", "99999.00")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Idempotency key was already used with a different request"));

        assertEquals(1, countRows("settlement"));
    }

    @Test
    void shouldRejectSettlingTheSameReceivableWithAnotherKey() throws Exception {
        mockMvc.perform(settlementRequest("first-key-0001", TRADE_BILL_IN_USD))
                .andExpect(status().isCreated());

        mockMvc.perform(settlementRequest("second-key-0002", TRADE_BILL_IN_USD))
                .andExpect(status().isConflict());

        assertEquals(1, countRows("settlement"));
    }

    @Test
    void shouldValidateTheIdempotencyKeyHeader() throws Exception {
        mockMvc.perform(post("/settlements").contentType(MediaType.APPLICATION_JSON).content(TRADE_BILL_IN_USD))
                .andExpect(status().isBadRequest());

        mockMvc.perform(settlementRequest("short", TRADE_BILL_IN_USD))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.idempotencyKey").exists());

        assertEquals(0, countRows("settlement"));
    }

    @Test
    void shouldNotPersistAnythingWhenTheRequestIsRejected() throws Exception {
        mockMvc.perform(settlementRequest("unknown-assignor", TRADE_BILL_IN_USD.replace("\"assignorId\": 1", "\"assignorId\": 999")))
                .andExpect(status().isNotFound());

        mockMvc.perform(settlementRequest("past-due-date-01", TRADE_BILL_IN_USD.replace("2026-12-14", "2026-09-14")))
                .andExpect(status().isUnprocessableContent());

        mockMvc.perform(settlementRequest("blank-document-1", TRADE_BILL_IN_USD.replace("DUP-0001", " ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.documentNumber").exists());

        assertEquals(0, countRows("settlement"));
        assertEquals(0, countRows("receivable"));
    }

    @Test
    void shouldCreateASingleSettlementWhenTheSameKeyArrivesConcurrently() throws Exception {
        SettlementRequestDTO settlementRequestDTO = tradeBillRequest("DUP-CONCURRENT");

        List<Future<SettlementResult>> futures = runConcurrently(
                index -> settlementService.settle("concurrent-key-0001", settlementRequestDTO)
        );

        List<SettlementResult> settlementResults = new ArrayList<>();
        for (Future<SettlementResult> future : futures) {
            settlementResults.add(future.get());
        }

        Set<Long> settlementIds = settlementResults.stream()
                .map(settlementResult -> settlementResult.settlement().id())
                .collect(Collectors.toSet());
        long createdCount = settlementResults.stream()
                .filter(settlementResult -> !settlementResult.replayed())
                .count();

        assertEquals(1, settlementIds.size());
        assertEquals(1, createdCount);
        assertEquals(1, countRows("settlement"));
    }

    @Test
    void shouldAllowOnlyOneSettlementWhenDifferentKeysRaceForTheSameReceivable() throws Exception {
        SettlementRequestDTO settlementRequestDTO = tradeBillRequest("DUP-RACE");

        List<Future<SettlementResult>> futures = runConcurrently(
                index -> settlementService.settle("race-key-" + index + "-abcdef", settlementRequestDTO)
        );

        int successCount = 0;
        int conflictCount = 0;
        for (Future<SettlementResult> future : futures) {
            try {
                future.get();
                successCount++;
            } catch (ExecutionException exception) {
                assertInstanceOf(ConflictException.class, exception.getCause());
                conflictCount++;
            }
        }

        assertEquals(1, successCount);
        assertEquals(CONCURRENT_REQUESTS - 1, conflictCount);
        assertEquals(1, countRows("settlement"));
    }

    private RequestBuilder settlementRequest(String idempotencyKey, String body) {
        return post("/settlements")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    private SettlementRequestDTO tradeBillRequest(String documentNumber) {
        return new SettlementRequestDTO(
                1L,
                documentNumber,
                ReceivableType.TRADE_BILL,
                new BigDecimal("100000.00"),
                LocalDate.parse("2026-12-14"),
                CurrencyCode.BRL
        );
    }

    private List<Future<SettlementResult>> runConcurrently(IndexedTask indexedTask) throws InterruptedException {
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Future<SettlementResult>> futures = new ArrayList<>();

        try (ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_REQUESTS)) {
            for (int index = 0; index < CONCURRENT_REQUESTS; index++) {
                int taskIndex = index;
                Callable<SettlementResult> callable = () -> {
                    startSignal.await();
                    return indexedTask.run(taskIndex);
                };
                futures.add(executorService.submit(callable));
            }
            startSignal.countDown();
            executorService.shutdown();
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        }
        return futures;
    }

    @FunctionalInterface
    private interface IndexedTask {
        SettlementResult run(int index);
    }
}
