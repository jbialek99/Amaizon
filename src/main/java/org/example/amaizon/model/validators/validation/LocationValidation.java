package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;


public class LocationValidation {


    public static final TextSpec NAME_SPEC = TextSpec.length(2,50);

    public static final ValidationKeys<TextValueObjectValidationException.Reason> NAME_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.location.name.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.location.name.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.location.name.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.location.name.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.location.name.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.location.name.invalid"
            )
    );

    public static final String KEY_COUNTRY_IS_NULL = "validation.location.country.is_null";
    public static final String KEY_TYPE_IS_NULL = "validation.location.type.is_null";
}
