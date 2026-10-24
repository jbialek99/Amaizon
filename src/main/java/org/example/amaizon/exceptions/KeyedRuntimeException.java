package org.example.amaizon.exceptions;

import lombok.Getter;

@Getter
public abstract class KeyedRuntimeException extends RuntimeException {

    private static final Object[] EMPTY_ARGS = {};

    private final String keyMessage;
    private final Object[] args;
    private final String field; // can be null


    protected KeyedRuntimeException(String keyMessage, Object... args) {
        this(keyMessage, null, args);
    }


    protected KeyedRuntimeException(String keyMessage, String field, Object... args) {
        super(keyMessage);
        this.keyMessage = keyMessage;
        this.field = field;
        this.args = (args == null) ? EMPTY_ARGS : args;
    }

}
