package org.example.amaizon.exceptions.phoneNumber;

import org.example.amaizon.exceptions.KeyedRuntimeException;


public class PhoneNumberValidationException extends KeyedRuntimeException {
    public PhoneNumberValidationException(String message, Object... args) {
        super(message, args);
    }
}
