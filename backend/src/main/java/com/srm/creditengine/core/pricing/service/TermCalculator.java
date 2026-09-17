package com.srm.creditengine.core.pricing.service;

import com.srm.creditengine.core.pricing.exception.InvalidPricingInputException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class TermCalculator {

    private TermCalculator() {
    }

    public static int monthsUntil(LocalDate referenceDate, LocalDate dueDate) {
        if (!dueDate.isAfter(referenceDate)) {
            throw new InvalidPricingInputException("Due date must be after " + referenceDate);
        }

        long fullMonths = ChronoUnit.MONTHS.between(referenceDate, dueDate);
        if (referenceDate.plusMonths(fullMonths).isBefore(dueDate)) {
            fullMonths++;
        }
        return Math.toIntExact(fullMonths);
    }
}
