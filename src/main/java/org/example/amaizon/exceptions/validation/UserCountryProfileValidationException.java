package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;



public class UserCountryProfileValidationException extends KeyedRuntimeException {
    public UserCountryProfileValidationException(String key, Object...args) {
        super(key,args);
    }

}
