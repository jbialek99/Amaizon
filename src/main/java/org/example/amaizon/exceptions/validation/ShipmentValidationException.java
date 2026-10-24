package org.example.amaizon.exceptions.validation;

public class ShipmentValidationException extends RuntimeException {
    public ShipmentValidationException(String key, Object... args) {
        super(key);
    }
}
