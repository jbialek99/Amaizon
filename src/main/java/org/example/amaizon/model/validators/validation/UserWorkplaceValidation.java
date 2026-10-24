package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.regex.Pattern;

public class UserWorkplaceValidation {
    public static final String KEY_USER_IS_NULL = "validation.user_workplaces.user.is_null";
    public static final String KEY_WORKPLACE_IS_NULL = "validation.user_workplaces.workplace.is_null";

    public static TextSpec POSITION_SPEC = TextSpec.pattern(2, 50, Pattern.compile(""));

    public static ValidationKeys<TextValueObjectValidationException.Reason> POSITION_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.user_workplaces.position.is_null",
                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.user_workplaces.position.is_empty",
                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.user_workplaces.position.too_short",
                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.user_workplaces.position.too_long",
                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.user_workplaces.position.invalid_length",
                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.user_workplaces.position.invalid"

            ));
}
