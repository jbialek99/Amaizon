package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class UserValidationException extends KeyedRuntimeException {
    public UserValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
