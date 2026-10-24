package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.regex.Pattern;

public class CountryValidation {

    public static final String KEY_CURRENCY_IS_NULL = "validation.countries.currency.is_null";

    public static final String KEY_ACTIVATE_WITHOUT_DOMAIN = "validation.country.activate_without_domain";
    public static final String KEY_ACTIVE_WITH_INACTIVE_DOMAIN = "validation.country.active_with_inactive_domain";
    public static final String KEY_DEACTIVATE_WITH_ACTIVE_DOMAIN = "validation.country.deactivate_with_active_domain";

    public static TextSpec LANG_CODE_SPEC = TextSpec.fixedWithPattern(5, Pattern.compile("^[a-z]{2}-[A-Z]{2}$"));

    public static final ValidationKeys<TextValueObjectValidationException.Reason> LANG_CODE_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.country.languageCode.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.country.languageCode.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.country.languageCode.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.country.languageCode.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.country.languageCode.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.country.languageCode.invalid"
            )
    );

    public static final TextSpec CODE_SPEC =
            TextSpec.fixedWithPattern(2, Pattern.compile("^[A-Z]{2}$"));

    public static final ValidationKeys<TextValueObjectValidationException.Reason> CODE_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.country.code.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.country.code.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.country.code.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.country.code.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.country.code.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.country.code.invalid"
            )
    );


    public static final TextSpec NAME_SPEC =
            TextSpec.pattern(2, 100, Pattern.compile("^[\\p{L} ]+$"));


    public static final ValidationKeys<TextValueObjectValidationException.Reason> NAME_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.country.name.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.country.name.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.country.name.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.country.name.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.country.name.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.country.name.invalid"
            )
    );

}
