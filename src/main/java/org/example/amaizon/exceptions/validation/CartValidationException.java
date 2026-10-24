package org.example.amaizon.exceptions.validation;

public class CartValidationException extends RuntimeException {
    public CartValidationException(String key, Object... args) {
        super(key);
    }
}
