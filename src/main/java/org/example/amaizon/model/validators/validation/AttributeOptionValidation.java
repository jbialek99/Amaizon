package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.intNumber.IntValueObjectValidationException;
import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.intNumber.IntSpec;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.regex.Pattern;

public class AttributeOptionValidation {

    public static final String KEY_ATTRIBUTE_DEFINITION_NULL = "validation.attribute_options.attributeDefinition.is_null";
    public static final String KEY_TRANSLATION_ALREADY_EXISTS = "validation.attribute_options.translation.already_exists";
    public static final String KEY_TRANSLATION_NOT_FOUND = "validation.attribute_options.translation.not_found";

    public static final String KEY_LANG_CODE_NULL = "validation.attribute_options.languageCode.is_null";

    public static TextSpec CODE_SPEC = TextSpec.pattern(1, 50, Pattern.compile("^[A-Za-z0-9]+$"));

    public static ValidationKeys<TextValueObjectValidationException.Reason> CODE_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.attribute_options.code.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.attribute_options.code.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.attribute_options.code.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.attribute_options.code.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.attribute_options.code.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.attribute_options.code.invalid"

            ));

    public static IntSpec DISPLAY_SPEC = new IntSpec(1,1000);

    public static ValidationKeys<IntValueObjectValidationException.Reason> DISPLAY_KEYS = new ValidationKeys<>(
            Map.of(
                    IntValueObjectValidationException.Reason.NULL,
                    "validation.attribute_options.displayOrder.is_null",

                    IntValueObjectValidationException.Reason.TOO_SMALL,
                    "validation.attribute_options.displayOrder.too_small",

                    IntValueObjectValidationException.Reason.TOO_LARGE,
                    "validation.attribute_options.displayOrder.too_large"
            ));


}
