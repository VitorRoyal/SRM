package com.srm.creditengine.core.pricing.service;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.pricing.dto.PricingInput;
import com.srm.creditengine.core.pricing.dto.PricingResult;
import com.srm.creditengine.core.pricing.exception.InvalidPricingInputException;
import com.srm.creditengine.core.pricing.strategy.PostDatedCheckPricingStrategy;
import com.srm.creditengine.core.pricing.strategy.ReceivablePricingStrategy;
import com.srm.creditengine.core.pricing.strategy.TradeBillPricingStrategy;
import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PricingEngineTest {

    private static final BigDecimal GOLDEN_BASE_RATE = new BigDecimal("0.01");
    private static final BigDecimal GOLDEN_EXCHANGE_RATE = new BigDecimal("5.4321");

    private final PricingEngine pricingEngine = new PricingEngine(List.of(
            new TradeBillPricingStrategy(),
            new PostDatedCheckPricingStrategy()
    ));

    @ParameterizedTest(name = "{0}")
    @CsvSource(nullValues = "NULL", value = {
            "C1, TRADE_BILL,       100000.00, 3, BRL, NULL,   92859.94, 7140.06, 92859.94",
            "C2, POST_DATED_CHECK, 25000.00,  2, BRL, NULL,   23337.77, 1662.23, 23337.77",
            "C3, TRADE_BILL,       100000.00, 3, USD, 5.4321, 92859.94, 7140.06, 17094.67"
    })
    void shouldReproduceGoldenCasesToTheCent(
            String caseId,
            ReceivableType receivableType,
            BigDecimal faceValue,
            int termInMonths,
            CurrencyCode paymentCurrency,
            BigDecimal exchangeRate,
            BigDecimal expectedPresentValueBrl,
            BigDecimal expectedDiscountBrl,
            BigDecimal expectedPaymentAmount
    ) {
        PricingResult pricingResult = pricingEngine.price(new PricingInput(
                receivableType, faceValue, termInMonths, GOLDEN_BASE_RATE, paymentCurrency, exchangeRate
        ));

        assertEquals(expectedPresentValueBrl, pricingResult.presentValueBrl());
        assertEquals(expectedDiscountBrl, pricingResult.discountBrl());
        assertEquals(expectedPaymentAmount, pricingResult.paymentAmount());
        assertEquals(paymentCurrency, pricingResult.paymentCurrency());
    }

    @Test
    void shouldConvertThePresentValueOnlyAfterRoundingIt() {
        PricingResult pricingResult = pricingEngine.price(new PricingInput(
                ReceivableType.TRADE_BILL, new BigDecimal("1022.00"), 2, GOLDEN_BASE_RATE, CurrencyCode.USD, GOLDEN_EXCHANGE_RATE
        ));

        assertEquals(new BigDecimal("972.75"), pricingResult.presentValueBrl());
        assertEquals(new BigDecimal("179.07"), pricingResult.paymentAmount());
    }

    @ParameterizedTest(name = "{0} uses spread {1}")
    @CsvSource({
            "TRADE_BILL,       0.015",
            "POST_DATED_CHECK, 0.025"
    })
    void shouldApplyTheSpreadOfEachReceivableType(ReceivableType receivableType, BigDecimal expectedSpread) {
        PricingResult pricingResult = pricingEngine.price(new PricingInput(
                receivableType, new BigDecimal("1000.00"), 1, GOLDEN_BASE_RATE, CurrencyCode.BRL, null
        ));

        assertEquals(expectedSpread, pricingResult.monthlySpread());
    }

    @Test
    void shouldReturnExactValueWhenDivisionIsExact() {
        PricingResult pricingResult = pricingEngine.price(new PricingInput(
                ReceivableType.TRADE_BILL, new BigDecimal("1025.00"), 1, GOLDEN_BASE_RATE, CurrencyCode.BRL, null
        ));

        assertEquals(new BigDecimal("1000.00"), pricingResult.presentValueBrl());
        assertEquals(new BigDecimal("25.00"), pricingResult.discountBrl());
        assertNull(pricingResult.exchangeRate());
    }

    @Test
    void shouldKeepCentPrecisionForLargeFaceValuesAndLongTerms() {
        PricingResult pricingResult = pricingEngine.price(new PricingInput(
                ReceivableType.POST_DATED_CHECK,
                new BigDecimal("99999999999999999.99"),
                360,
                GOLDEN_BASE_RATE,
                CurrencyCode.BRL,
                null
        ));

        assertEquals(2, pricingResult.presentValueBrl().scale());
        assertEquals(
                new BigDecimal("99999999999999999.99"),
                pricingResult.presentValueBrl().add(pricingResult.discountBrl())
        );
    }

    @ParameterizedTest(name = "face value {0}")
    @CsvSource({"0", "-0.01"})
    void shouldRejectNonPositiveFaceValue(BigDecimal faceValue) {
        PricingInput pricingInput = new PricingInput(
                ReceivableType.TRADE_BILL, faceValue, 1, GOLDEN_BASE_RATE, CurrencyCode.BRL, null
        );

        assertThrows(InvalidPricingInputException.class, () -> pricingEngine.price(pricingInput));
    }

    @Test
    void shouldRejectForeignCurrencyPaymentWithoutExchangeRate() {
        PricingInput pricingInput = new PricingInput(
                ReceivableType.TRADE_BILL, new BigDecimal("1000.00"), 1, GOLDEN_BASE_RATE, CurrencyCode.USD, null
        );

        assertThrows(InvalidPricingInputException.class, () -> pricingEngine.price(pricingInput));
    }

    @Test
    void shouldRejectTermShorterThanOneMonth() {
        PricingInput pricingInput = new PricingInput(
                ReceivableType.TRADE_BILL, new BigDecimal("1000.00"), 0, GOLDEN_BASE_RATE, CurrencyCode.BRL, null
        );

        assertThrows(InvalidPricingInputException.class, () -> pricingEngine.price(pricingInput));
    }

    @Test
    void shouldRefuseTwoStrategiesForTheSameReceivableType() {
        List<ReceivablePricingStrategy> duplicatedStrategies = List.of(
                new TradeBillPricingStrategy(),
                new TradeBillPricingStrategy()
        );

        assertThrows(IllegalStateException.class, () -> new PricingEngine(duplicatedStrategies));
    }
}
