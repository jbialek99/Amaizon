package org.example.amaizon.model.validators.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailNotTakenValidator.class)
public @interface EmailNotTaken {

    String message() default "{validation.user.email.already_used}";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
