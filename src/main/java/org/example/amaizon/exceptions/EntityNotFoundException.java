package org.example.amaizon.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EntityNotFoundException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;
    private final HttpStatus status;

    public EntityNotFoundException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
        this.status = HttpStatus.NOT_FOUND;
    }

}
