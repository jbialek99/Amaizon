package org.example.amaizon.exceptions.text;

import lombok.Getter;

@Getter
public class TextValueObjectValidationException extends RuntimeException {

    public enum Reason {
        NULL,
        EMPTY,
        TOO_LONG,
        TOO_SHORT,
        INVALID_LENGTH,
        INVALID_FORMAT
    }

    private final Reason reason;
    private final Object[] args;

    public TextValueObjectValidationException(Reason reason, Object... args) {
        super(reason.name());
        this.reason = reason;
        this.args = (args == null) ? new Object[0] : args;
    }

}
