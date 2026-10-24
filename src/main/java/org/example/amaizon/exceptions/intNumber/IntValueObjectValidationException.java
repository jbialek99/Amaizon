package org.example.amaizon.exceptions.intNumber;

import lombok.Getter;


@Getter
public class IntValueObjectValidationException extends RuntimeException {
    public enum Reason {
        NULL,
        TOO_SMALL,
        TOO_LARGE
    }

    private final Reason reason;
    private final Object[] args;


    public IntValueObjectValidationException(Reason reason, Object... args) {
        super(reason.name());
        this.reason = reason;
        this.args = args;
    }

}
