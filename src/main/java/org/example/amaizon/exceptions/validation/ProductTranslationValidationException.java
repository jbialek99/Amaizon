package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class ProductTranslationValidationException extends KeyedRuntimeException {
    public ProductTranslationValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
