package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.regex.Pattern;

public class AttributeOptionTranslationValidation {

    public static final String KEY_ATTRIBUTE_OPTION_NULL = "validation.attribute_option_translation.attributeOption.is_null";

    public static TextSpec LANG_CODE_SPEC = TextSpec.fixedWithPattern(5, Pattern.compile("^[a-z]{2}-[A-Z]{2}$"));

    public static ValidationKeys<TextValueObjectValidationException.Reason> LANG_CODE_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.attribute_option_translation.languageCode.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.attribute_option_translation.languageCode.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.attribute_option_translation.languageCode.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.attribute_option_translation.languageCode.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.attribute_option_translation.languageCode.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.attribute_option_translation.languageCode.invalid"

            ));


    public static TextSpec DISPLAY_NAME_SPEC =
            TextSpec.pattern(
                    1,
                    50,
                    Pattern.compile("^[\\p{L}0-9 .,'-]+$")
            );

    public static ValidationKeys<TextValueObjectValidationException.Reason> DISPLAY_NAME_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.attribute_option_translation.displayName.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.attribute_option_translation.displayName.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.attribute_option_translation.displayName.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.attribute_option_translation.displayName.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.attribute_option_translation.displayName.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.attribute_option_translation.displayName.invalid"

            ));


}
