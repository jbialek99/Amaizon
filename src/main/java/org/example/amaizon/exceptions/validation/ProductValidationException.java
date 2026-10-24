package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class ProductValidationException extends KeyedRuntimeException {
    public ProductValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
