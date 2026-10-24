package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class UnitValidationException extends KeyedRuntimeException {
    public UnitValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
