package org.example.amaizon.model.validators.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ProductSkuValidator implements ConstraintValidator<ValidProductSku, String> {

    private static final int MIN = 8;
    private static final int MAX = 12;
    private static final Pattern P = Pattern.compile("^(?:[A-Z]{1,3}[0-9]+|[0-9]+)$");

    @Override
    public boolean isValid(String raw, ConstraintValidatorContext ctx) {
        if (raw == null) {
            violation(ctx, "validation.product.sku.is_null");
            return false;
        }

        String s = raw.trim();
        if (s.isEmpty()) {
            violation(ctx, "validation.product.sku.is_empty");
            return false;
        }

        if (s.length() < MIN) {
            violation(ctx, "validation.product.sku.too_short");
            return false;
        }

        if (s.length() > MAX) {
            violation(ctx, "validation.product.sku.too_long");
            return false;
        }

        if (!P.matcher(s).matches()) {
            violation(ctx, "validation.product.sku.invalid");
            return false;
        }

        return true;
    }

    private static void violation(ConstraintValidatorContext ctx, String key) {
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate("{" + key + "}")
                .addConstraintViolation();
    }
}
