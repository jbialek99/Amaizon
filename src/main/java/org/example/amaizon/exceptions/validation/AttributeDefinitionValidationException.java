package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class AttributeDefinitionValidationException extends KeyedRuntimeException {
    public AttributeDefinitionValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
