package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class CategoryValidationException extends KeyedRuntimeException {
    public CategoryValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
