package com.srm.creditengine.core.pricing.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonetaryPolicyTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "0.125,   0.12",
            "0.135,   0.14",
            "0.1251,  0.13",
            "-0.125, -0.12",
            "7,       7.00"
    })
    void shouldRoundMoneyHalfEvenToTwoDecimals(BigDecimal rawValue, BigDecimal expectedRoundedValue) {
        assertEquals(expectedRoundedValue, MonetaryPolicy.roundMoney(rawValue));
    }
}
