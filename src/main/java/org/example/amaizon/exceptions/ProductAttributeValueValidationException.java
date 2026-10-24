package org.example.amaizon.exceptions;

public class ProductAttributeValueValidationException extends RuntimeException {
    private final String keyMessage;
    private final Object[] args;

    public ProductAttributeValueValidationException(String keyMessage, Object... args) {
        super(keyMessage);
        this.keyMessage = keyMessage;
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }

    public String getKeyMessage() {
        return keyMessage;
    }
}
