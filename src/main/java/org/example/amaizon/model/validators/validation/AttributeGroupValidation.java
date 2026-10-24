package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.intNumber.IntValueObjectValidationException;
import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.intNumber.IntSpec;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.regex.Pattern;

public class AttributeGroupValidation {


    public static final String KEY_LANGUAGE_CODE_NULL = "validation.attribute_groups.languageCode.is_null";
    public static TextSpec CODE_SPEC = TextSpec.pattern(1, 50, Pattern.compile("^[a-z][a-z0-9_]*$"));

    public static ValidationKeys<TextValueObjectValidationException.Reason> CODE_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.attribute_groups.code.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.attribute_groups.code.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.attribute_groups.code.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.attribute_groups.code.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.attribute_groups.code.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.attribute_groups.code.invalid"

            ));


    public static final String KEY_TRANSLATION_ALREADY_EXISTS =
            "validation.attribute_groups.translation.already_exists";

    public static final String KEY_TRANSLATION_NOT_FOUND =
            "validation.attribute_groups.translation.not_found";

    public static final IntSpec DISPLAY_SPEC = IntSpec.range(1,1000);

    public static final ValidationKeys<IntValueObjectValidationException.Reason> DISPLAY_KEYS = new ValidationKeys<>(
            Map.of(
                    IntValueObjectValidationException.Reason.NULL,
                    "validation.attribute_groups.displayOrder.is_null",
                    IntValueObjectValidationException.Reason.TOO_SMALL,
                    "validation.attribute_groups.displayOrder.too_small",
                    IntValueObjectValidationException.Reason.TOO_LARGE,
                    "validation.attribute_groups.displayOrder.too_large"
            ));

    public static final String KEY_ATTRIBUTE_DEFINITION_NULL = "validation.attribute_groups.attributeDefinition.is_null";

    public static final String KEY_ITEM_ALREADY_EXISTS = "validation.attribute_groups.item.already_exists";
    public static final String KEY_ITEM_NOT_FOUND = "validation.attribute_groups.item.not_found";
}
