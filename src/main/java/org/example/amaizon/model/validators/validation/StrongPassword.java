package org.example.amaizon.model.validators.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;


import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    int min() default 8;
    int max() default 100;
    int minDigits() default 2;

    String message() default "{validation.user.password.weak}";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
