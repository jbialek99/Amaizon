package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class CategoryTranslationValidationException extends KeyedRuntimeException {
    public CategoryTranslationValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
