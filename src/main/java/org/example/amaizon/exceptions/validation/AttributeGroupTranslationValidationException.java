package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class AttributeGroupTranslationValidationException extends KeyedRuntimeException {
    public AttributeGroupTranslationValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
