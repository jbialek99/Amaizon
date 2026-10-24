package org.example.amaizon.exceptions.validation;
import org.example.amaizon.exceptions.KeyedRuntimeException;

public class LocationValidationException extends KeyedRuntimeException {
    public LocationValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
