package org.example.amaizon.exceptions;

public class EmailAlreadyUsedException extends KeyedRuntimeException {

    public EmailAlreadyUsedException(String email) {
        super("validation.user.email.already_used", "email", email);
    }
}
