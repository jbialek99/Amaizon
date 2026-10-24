package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.regex.Pattern;

public final class ProductTranslationValidation {

    private ProductTranslationValidation() {}

    public static final String KEY_PRODUCT_COUNTRY_IS_NULL =
            "validation.product_translations.productCountry.is_null";

    public static final TextSpec LANG_CODE_SPEC =
            TextSpec.fixedWithPattern(5, Pattern.compile("^[a-z]{2}-[A-Z]{2}$"));

    public static final ValidationKeys<TextValueObjectValidationException.Reason> LANG_CODE_KEYS =
            new ValidationKeys<>(
                    Map.of(
                            TextValueObjectValidationException.Reason.NULL,
                            "validation.product_translations.languageCode.is_null",

                            TextValueObjectValidationException.Reason.EMPTY,
                            "validation.product_translations.languageCode.is_empty",

                            TextValueObjectValidationException.Reason.TOO_SHORT,
                            "validation.product_translations.languageCode.too_short",

                            TextValueObjectValidationException.Reason.TOO_LONG,
                            "validation.product_translations.languageCode.too_long",

                            TextValueObjectValidationException.Reason.INVALID_LENGTH,
                            "validation.product_translations.languageCode.invalid_length",

                            TextValueObjectValidationException.Reason.INVALID_FORMAT,
                            "validation.product_translations.languageCode.invalid"
                    )
            );

    public static final TextSpec DISPLAY_NAME_SPEC =
            TextSpec.pattern(1, 50, Pattern.compile("^[\\p{L}0-9 .,'-]+$"));

    public static final ValidationKeys<TextValueObjectValidationException.Reason> DISPLAY_NAME_KEYS =
            new ValidationKeys<>(
                    Map.of(
                            TextValueObjectValidationException.Reason.NULL,
                            "validation.product_translations.displayName.is_null",

                            TextValueObjectValidationException.Reason.EMPTY,
                            "validation.product_translations.displayName.is_empty",

                            TextValueObjectValidationException.Reason.TOO_SHORT,
                            "validation.product_translations.displayName.too_short",

                            TextValueObjectValidationException.Reason.TOO_LONG,
                            "validation.product_translations.displayName.too_long",

                            TextValueObjectValidationException.Reason.INVALID_LENGTH,
                            "validation.product_translations.displayName.invalid_length",

                            TextValueObjectValidationException.Reason.INVALID_FORMAT,
                            "validation.product_translations.displayName.invalid"
                    )
            );

    public static final TextSpec DESCRIPTION_SPEC = TextSpec.max(5000);

    public static final ValidationKeys<TextValueObjectValidationException.Reason> DESCRIPTION_KEYS =
            new ValidationKeys<>(
                    Map.of(
                            TextValueObjectValidationException.Reason.NULL,
                            "validation.product_translations.description.is_null",

                            TextValueObjectValidationException.Reason.EMPTY,
                            "validation.product_translations.description.is_empty",

                            TextValueObjectValidationException.Reason.TOO_SHORT,
                            "validation.product_translations.description.too_short",

                            TextValueObjectValidationException.Reason.TOO_LONG,
                            "validation.product_translations.description.too_long",

                            TextValueObjectValidationException.Reason.INVALID_LENGTH,
                            "validation.product_translations.description.invalid_length",

                            TextValueObjectValidationException.Reason.INVALID_FORMAT,
                            "validation.product_translations.description.invalid"
                    )
            );

    public static final TextSpec SLUG_SPEC =
            TextSpec.pattern(3, 100, Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$"));

    public static final ValidationKeys<TextValueObjectValidationException.Reason> SLUG_KEYS =
            new ValidationKeys<>(
                    Map.of(
                            TextValueObjectValidationException.Reason.NULL,
                            "validation.product_translations.slug.is_null",

                            TextValueObjectValidationException.Reason.EMPTY,
                            "validation.product_translations.slug.is_empty",

                            TextValueObjectValidationException.Reason.TOO_SHORT,
                            "validation.product_translations.slug.too_short",

                            TextValueObjectValidationException.Reason.TOO_LONG,
                            "validation.product_translations.slug.too_long",

                            TextValueObjectValidationException.Reason.INVALID_LENGTH,
                            "validation.product_translations.slug.invalid_length",

                            TextValueObjectValidationException.Reason.INVALID_FORMAT,
                            "validation.product_translations.slug.invalid"
                    )
            );
}
