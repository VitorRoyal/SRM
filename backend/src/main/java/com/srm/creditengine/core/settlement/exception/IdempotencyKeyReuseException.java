package com.srm.creditengine.core.settlement.exception;

import com.srm.creditengine.shared.exception.BusinessRuleException;

public class IdempotencyKeyReuseException extends BusinessRuleException {

    public IdempotencyKeyReuseException(String message) {
        super(message);
    }
}
