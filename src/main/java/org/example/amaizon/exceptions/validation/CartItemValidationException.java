package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class CartItemValidationException extends KeyedRuntimeException {
    public CartItemValidationException(String message, Object...args) {
        super(message, args);
    }
}
