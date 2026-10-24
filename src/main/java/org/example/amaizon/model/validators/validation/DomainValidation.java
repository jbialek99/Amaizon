package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.regex.Pattern;

public class DomainValidation {

    public static final String KEY_COUNTRY_IS_NULL = "validation.domain.country.is_null";
    public static final String KEY_COUNTRY_ALREADY_HAS_DOMAIN = "validation.domain.country.already_has_domain";

    public static final TextSpec CCTLD_SPEC =
            TextSpec.pattern(2, 5, Pattern.compile("^[a-z]+$"));

    public static final ValidationKeys<TextValueObjectValidationException.Reason> CCTLD_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.domain.ccTld.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.domain.ccTld.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.domain.ccTld.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.domain.ccTld.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.domain.ccTld.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.domain.ccTld.invalid"
            )
    );


    public static final TextSpec NAME_SPEC =
            TextSpec.pattern(1, 50, Pattern.compile("^[\\p{L}0-9 .'-]+$"));

    public static final ValidationKeys<TextValueObjectValidationException.Reason> NAME_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.domain.name.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.domain.name.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.domain.name.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.domain.name.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.domain.name.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.domain.name.invalid"
            )
    );
}
