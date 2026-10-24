package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.regex.Pattern;

public class RoleValidation {
    public static final String KEY_COUNTRY_NULL = "validation.role.country.is_null";

    public static final String KEY_GLOBAL_COUNTRY_MUST_BE_NULL = "validation.role.global.country_must_be_null";

    public static final String KEY_SCOPE_NULL =  "validation.role.scope.is_null";
    public static final String KEY_KEY_NULL = "validation.role.key.is_null";

    public static final TextSpec DESC_SPEC = TextSpec.length(1,5000);

    public static final ValidationKeys<TextValueObjectValidationException.Reason> DESC_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.role.description.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.role.description.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.role.description.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.role.description.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.role.description.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.role.description.invalid"
            )
    );


    public static TextSpec NAME_SPEC = TextSpec.pattern(
            1,
            50,
            Pattern.compile("^[A-Za-z]+([ _-][A-Za-z]+)*$")
    );

    public static ValidationKeys<TextValueObjectValidationException.Reason> NAME_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.role.name.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.role.name.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.role.name.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.role.name.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.role.name.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.role.name.invalid"

            )
    );

}
