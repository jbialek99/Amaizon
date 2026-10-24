package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.regex.Pattern;

public class CategoryTranslationValidation {


    public static final String KEY_CATEGORY_COUNTRIES_IS_NULL =
            "validation.category_translations.category_countries.is_null";

    public static TextSpec LANG_CODE_SPEC = TextSpec.fixedWithPattern(5, Pattern.compile("^[a-z]{2}-[A-Z]{2}$"));

    public static ValidationKeys<TextValueObjectValidationException.Reason> LANG_CODE_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.category_translations.languageCode.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.category_translations.languageCode.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.category_translations.languageCode.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.category_translations.languageCode.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.category_translations.languageCode.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.category_translations.languageCode.invalid"

            )
    );

    public static TextSpec DISPLAY_NAME_SPEC =
            TextSpec.pattern(
                    1,
                    50,
                    Pattern.compile("^[\\p{L}0-9 .,'-]+$")
            );
    public static ValidationKeys<TextValueObjectValidationException.Reason> DISPLAY_NAME_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.category_translations.displayName.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.category_translations.displayName.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.category_translations.displayName.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.category_translations.displayName.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.category_translations.displayName.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.category_translations.displayName.invalid"

            )
    );


    public static TextSpec DESCRIPTION_SPEC = TextSpec.max(5000);

    public static ValidationKeys<TextValueObjectValidationException.Reason> DESCRIPTION_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.category_translations.description.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.category_translations.description.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.category_translations.description.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.category_translations.description.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.category_translations.description.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.category_translations.description.invalid"

            )
    );

    public static TextSpec SLUG_SPEC = TextSpec.pattern(3,100,Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$"));

    public static ValidationKeys<TextValueObjectValidationException.Reason> SLUG_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.category_translations.slug.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.category_translations.slug.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.category_translations.slug.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.category_translations.slug.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.category_translations.slug.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.category_translations.slug.invalid"

            )
    );


}
