package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class CurrencyValidationException extends KeyedRuntimeException {
    public CurrencyValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
