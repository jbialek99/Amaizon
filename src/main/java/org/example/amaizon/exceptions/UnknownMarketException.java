package org.example.amaizon.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class UnknownMarketException extends RuntimeException {

    private final String keyMessage;
    private final Object[] args;
    private final HttpStatus status;

    public UnknownMarketException(String keyMessage, HttpStatus status, Object... args) {
        super(keyMessage);
        this.keyMessage = keyMessage;
        this.status = status;
        this.args = args;
    }

    public String getKeyMessage() {
        return keyMessage;
    }

    public Object[] getArgs() {
        return args;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
