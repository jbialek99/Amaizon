package org.example.amaizon.exceptions.imageUrl;

import lombok.Getter;

@Getter
public class ImageUrlValueObjectValidationException extends RuntimeException {


    public enum Reason {
        NULL,
        EMPTY,
        TOO_SHORT,
        TOO_LONG,
        INVALID_URI,
        INVALID_SCHEME,
        HOST_NOT_ALLOWED,
        QUERY_NOT_ALLOWED,
        FRAGMENT_NOT_ALLOWED,
        INVALID_PATH,
        INVALID_EXTENSION
    }

    private final Reason reason;
    private final Object[] args;

    public ImageUrlValueObjectValidationException(Reason reason, Object... args) {
        super(reason.name());
        this.reason = reason;
        this.args = args;

    }

}
