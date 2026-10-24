package org.example.amaizon.exceptions.price;

import lombok.Getter;

@Getter
public class PriceValueObjectValidationException extends RuntimeException {

    public enum Reason {
        NULL,
        NEGATIVE_NOT_ALLOWED,
        INVALID_SCALE,
        TOO_SMALL,
        TOO_LARGE
    }

    private final Reason reason;
    private final Object[] args;

    public PriceValueObjectValidationException(Reason reason, Object... args) {
        super(reason.name());
        this.reason = reason;
        this.args = args;
    }


}
