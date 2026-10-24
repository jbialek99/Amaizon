package org.example.amaizon.exceptions.validation;

public class PaymentValidationException extends RuntimeException {
    public PaymentValidationException(String key, Object... args) {
        super(key);
    }
}
