package org.example.amaizon.model.validators.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private int min;
    private int max;
    private int minDigits;

    @Override
    public void initialize(StrongPassword a) {
        this.min = a.min();
        this.max = a.max();
        this.minDigits = a.minDigits();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {

        if (value == null || value.isBlank()) {
            return true;
        }

        int len = value.length();
        if (len < min || len > max) {
            violation(ctx, "{validation.user.password.invalid_length}");
            return false;
        }

        int digits = 0;
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) digits++;
        }

        if (digits < minDigits) {
            violation(ctx, "{validation.user.password.min_digits}");
            return false;
        }

        return true;
    }

    private void violation(ConstraintValidatorContext ctx, String template) {
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(template).addConstraintViolation();
    }
}
