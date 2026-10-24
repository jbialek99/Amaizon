package org.example.amaizon.exceptions.validation;


import org.example.amaizon.exceptions.KeyedRuntimeException;

public class CategoryCountryValidationException extends KeyedRuntimeException {
    public CategoryCountryValidationException(String keyMessage, Object... args) {
        super(keyMessage, args);
    }
}
