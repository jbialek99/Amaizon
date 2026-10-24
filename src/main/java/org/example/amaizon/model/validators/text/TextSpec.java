package org.example.amaizon.model.validators.text;

import java.util.regex.Pattern;

public record TextSpec(int min, int max, Pattern pattern) {

    public TextSpec {
        if (min < 0) {
            throw new IllegalArgumentException("min must be >= 0");
        }

        if (max <= 0) {
            throw new IllegalArgumentException("max must be > 0");
        }

        if (min > max) {
            throw new IllegalArgumentException("min cannot be greater than max");
        }

    }


    public static TextSpec length(int min, int max) {
        return new TextSpec(min, max, null);
    }


    public static TextSpec max(int max) {
        return new TextSpec(1, max, null);
    }


    public static TextSpec fixed(int length) {
        return new TextSpec(length, length, null);
    }

    public static TextSpec fixedWithPattern(int length, Pattern pattern) {
        return new TextSpec(length, length, pattern);
    }

    public static TextSpec pattern(int min, int max, Pattern pattern) {
        if (pattern == null)
            throw new IllegalArgumentException("pattern cannot be null");
        return new TextSpec(min, max, pattern);
    }

}
