package org.example.amaizon.exceptions;



public class ProductValidationException extends RuntimeException {

    private final String keyMessage;
    private final Object[] args;


    public ProductValidationException(String keyMessage, Object... args) {
        super(keyMessage);
        this.keyMessage = keyMessage;
        this.args = args;
    }

  public String getKeyMessage() {
    return keyMessage;
  }

  public Object[] getArgs() {
    return args;
  }
}
