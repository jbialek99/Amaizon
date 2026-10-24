package org.example.amaizon.exceptions.validation;


import org.example.amaizon.exceptions.KeyedRuntimeException;

public class DomainValidationException extends KeyedRuntimeException {
    public DomainValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
