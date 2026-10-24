package org.example.amaizon.exceptions.validation;


import org.example.amaizon.exceptions.KeyedRuntimeException;

public class AttributeGroupValidationException extends KeyedRuntimeException {
    public AttributeGroupValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
