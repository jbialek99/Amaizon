package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class ProductStockValidationException extends KeyedRuntimeException {
    public ProductStockValidationException(String key, Object... args) {
        super(key);
    }
}
