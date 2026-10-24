package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class CountryValidationException extends KeyedRuntimeException {
    public CountryValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
