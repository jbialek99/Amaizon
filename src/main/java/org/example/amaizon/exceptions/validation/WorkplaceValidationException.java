package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class WorkplaceValidationException extends KeyedRuntimeException {
    public WorkplaceValidationException(String key, Object... args) {
        super(key, args);
    }
}
