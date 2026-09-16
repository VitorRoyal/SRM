package com.srm.creditengine.core.pricing.exception;

import com.srm.creditengine.shared.exception.BusinessRuleException;

public class InvalidPricingInputException extends BusinessRuleException {

    public InvalidPricingInputException(String message) {
        super(message);
    }
}
