package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class ProductCountryValidationException extends KeyedRuntimeException {
    public ProductCountryValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
