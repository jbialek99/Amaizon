package org.example.amaizon.exceptions.validation;

import org.example.amaizon.exceptions.KeyedRuntimeException;

public class UserCountryMembershipValidationException extends KeyedRuntimeException {
    public UserCountryMembershipValidationException(String message, Object... args) {
        super(message, args);
    }
}
