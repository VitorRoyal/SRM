package com.srm.creditengine.core.pricing.service;

import com.srm.creditengine.core.pricing.exception.InvalidPricingInputException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TermCalculatorTest {

    @ParameterizedTest(name = "{0} -> {1} = {2} months")
    @CsvSource({
            "2026-09-14, 2026-09-15, 1",
            "2026-09-14, 2026-10-14, 1",
            "2026-09-14, 2026-10-15, 2",
            "2026-09-14, 2026-12-14, 3",
            "2026-01-31, 2026-02-28, 1",
            "2026-01-31, 2026-03-01, 2",
            "2026-09-14, 2027-09-14, 12"
    })
    void shouldCountCalendarMonthsRoundingPartialMonthsUp(LocalDate referenceDate, LocalDate dueDate, int expectedMonths) {
        assertEquals(expectedMonths, TermCalculator.monthsUntil(referenceDate, dueDate));
    }

    @ParameterizedTest(name = "due date {1}")
    @CsvSource({
            "2026-09-14, 2026-09-14",
            "2026-09-14, 2026-09-13"
    })
    void shouldRejectDueDateNotAfterReferenceDate(LocalDate referenceDate, LocalDate dueDate) {
        assertThrows(InvalidPricingInputException.class, () -> TermCalculator.monthsUntil(referenceDate, dueDate));
    }
}
