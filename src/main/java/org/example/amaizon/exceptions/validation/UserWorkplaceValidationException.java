package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class UserWorkplaceValidationException extends KeyedRuntimeException {
    public UserWorkplaceValidationException(String key, Object... args) {
        super(key, args);
    }
}
