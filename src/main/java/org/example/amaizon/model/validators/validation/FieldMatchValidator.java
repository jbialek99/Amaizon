package org.example.amaizon.model.validators.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.util.Objects;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {

    private String first;
    private String second;
    private String message;

    @Override
    public void initialize(FieldMatch a) {
        this.first = a.first();
        this.second = a.second();
        this.message = a.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext ctx) {
        if (value == null) return true;

        Object v1 = readField(value, first);
        Object v2 = readField(value, second);


        if (v1 == null || v2 == null) return true;
        if (v1 instanceof String s1 && s1.isBlank()) return true;
        if (v2 instanceof String s2 && s2.isBlank()) return true;

        boolean ok = Objects.equals(v1, v2);
        if (!ok) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(second)
                    .addConstraintViolation();
        }
        return ok;
    }

    private Object readField(Object target, String name) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            return null;
        }
    }
}
