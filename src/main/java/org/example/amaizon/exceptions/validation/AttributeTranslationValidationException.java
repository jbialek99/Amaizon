package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class AttributeTranslationValidationException extends KeyedRuntimeException {
    public AttributeTranslationValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
