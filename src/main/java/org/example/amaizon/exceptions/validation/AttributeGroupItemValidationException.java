package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class AttributeGroupItemValidationException extends KeyedRuntimeException {
    public AttributeGroupItemValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
