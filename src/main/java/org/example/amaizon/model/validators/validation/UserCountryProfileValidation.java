package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;

public class UserCountryProfileValidation {

    public static final String KEY_USER_IS_NULL = "validation.user_country_profiles.user.is_null";
    public static final String KEY_COUNTRY_IS_NULL = "validation.user_country_profiles.country.is_null";

    public static final TextSpec NAME_SPEC = TextSpec.length(2,50);

    public static final ValidationKeys<TextValueObjectValidationException.Reason> FIRST_NAME_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.user_country_profiles.firstName.is_null",
                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.user_country_profiles.firstName.is_empty",
                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.user_country_profiles.firstName.too_short",
                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.user_country_profiles.firstName.too_long",
                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.user_country_profiles.firstName.invalid_length",
                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.user_country_profiles.firstName.invalid"

            )
    );

    public static final ValidationKeys<TextValueObjectValidationException.Reason> LAST_NAME_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.user_country_profiles.lastName.is_null",
                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.user_country_profiles.lastName.is_empty",
                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.user_country_profiles.lastName.too_short",
                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.user_country_profiles.lastName.too_long",
                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.user_country_profiles.lastName.invalid_length",
                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.user_country_profiles.lastName.invalid"

            )
    );
}
