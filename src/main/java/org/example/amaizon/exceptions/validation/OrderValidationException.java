package org.example.amaizon.exceptions.validation;

public class OrderValidationException extends RuntimeException {
    public OrderValidationException(String key, Object... args) {
        super(build(key, args));
    }

    private static String build(String key, Object... args) {
        StringBuilder sb = new StringBuilder(key);
        if (args != null && args.length > 0) {
            sb.append(" [");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(args[i]);
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
