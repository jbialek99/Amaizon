package org.example.amaizon.model.validators.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ProductSkuValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidProductSku {
    String message() default "{validation.product.sku.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
