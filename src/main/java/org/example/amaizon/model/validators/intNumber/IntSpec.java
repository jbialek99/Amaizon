package org.example.amaizon.model.validators.intNumber;

public record IntSpec(int min, int max) {

    public IntSpec {
        if (min > max)
            throw new IllegalArgumentException("Minimum must be <= max");
    }



    public static IntSpec range(int min, int max) {
        return new IntSpec(min, max);
    }

    public static IntSpec min(int min) {
        return new IntSpec(min, Integer.MAX_VALUE);
    }

    public static IntSpec max(int max) {
        return new IntSpec(Integer.MIN_VALUE, max);
    }

}
