package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class CategoryAttributeAssignmentValidationException extends KeyedRuntimeException {
    public CategoryAttributeAssignmentValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
