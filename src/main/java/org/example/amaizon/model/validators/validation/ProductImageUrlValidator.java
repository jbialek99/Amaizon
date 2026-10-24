package org.example.amaizon.model.validators.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.amaizon.exceptions.imageUrl.ImageUrlValueObjectValidationException;
import org.example.amaizon.model.validators.imageUrl.ImageUrlValidator;

public class ProductImageUrlValidator implements ConstraintValidator<ValidProductImageUrl, String> {

    @Override
    public boolean isValid(String raw, ConstraintValidatorContext ctx) {

        if (raw == null || raw.isBlank()) return true;

        try {
            ImageUrlValidator.validate(
                    raw,
                    ProductValidation.IMAGE_URL_SPEC,
                    ImageUrlValidator.defaultNormalize()
            );
            return true;
        } catch (ImageUrlValueObjectValidationException ex) {
            String key = ProductValidation.IMAGE_URL_KEYS.resolve(ex.getReason());
            violation(ctx, key);
            return false;
        }
    }

    private static void violation(ConstraintValidatorContext ctx, String key) {
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate("{" + key + "}")
                .addConstraintViolation();
    }
}
