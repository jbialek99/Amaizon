package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class ProductAttributeValueValidationException extends KeyedRuntimeException {
    public ProductAttributeValueValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
