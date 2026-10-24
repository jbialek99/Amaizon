package org.example.amaizon.model.validators.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidCheckoutAddressValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCheckoutAddress {
    String message() default "{validation.checkout.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
