package org.example.amaizon.model.validators.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ProductImageUrlValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidProductImageUrl {
    String message() default "{validation.product.imageUrl.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
