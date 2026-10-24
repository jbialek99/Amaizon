package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class ProductPriceValidationException extends KeyedRuntimeException {
    public ProductPriceValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
