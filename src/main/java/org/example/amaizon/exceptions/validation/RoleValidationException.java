package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class RoleValidationException extends KeyedRuntimeException {
    public RoleValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
