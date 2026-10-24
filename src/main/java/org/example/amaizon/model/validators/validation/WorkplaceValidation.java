package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;

import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.regex.Pattern;

public class WorkplaceValidation {

    public static final String KEY_COUNTRY_IS_NULL = "validation.workplace.country.is_null";
    public static final String KEY_TYPE_IS_NULL = "validation.workplace.type.is_null";
    public static final String KEY_LOCATION_IS_NULL = "validation.workplace.location.is_null";
    public static final String KEY_LOCATION_COUNTRY_MISMATCH = "validation.workplace.location.country_mismatch";

    public static final TextSpec DISPLAY_NAME_SPEC = TextSpec.length(2, 70);

    public static final ValidationKeys<TextValueObjectValidationException.Reason> DISPLAY_NAME_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.workplace.displayName.is_null",
                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.workplace.displayName.is_empty",
                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.workplace.displayName.too_short",
                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.workplace.displayName.too_long",
                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.workplace.displayName.invalid_length",
                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.workplace.displayName.invalid"

            )
    );


    public static final TextSpec CODE_SPEC =
            TextSpec.fixedWithPattern(
                    11,
                    Pattern.compile("^[A-Z]{2}-[A-Z]{3}-\\d{4}$")
            );

    public static final ValidationKeys<TextValueObjectValidationException.Reason> CODE_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.workplace.code.is_null",
                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.workplace.code.is_empty",
                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.workplace.code.too_short",
                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.workplace.code.too_long",
                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.workplace.code.invalid_length",
                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.workplace.code.invalid"

            )
    );


}
