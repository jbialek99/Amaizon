package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class AttributeOptionValidationException extends KeyedRuntimeException {
    public AttributeOptionValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
