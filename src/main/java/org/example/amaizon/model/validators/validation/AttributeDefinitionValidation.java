package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.regex.Pattern;

public class AttributeDefinitionValidation {


    private AttributeDefinitionValidation() {
    }


    public static final TextSpec CODE_SPEC =
            TextSpec.pattern(1, 50, Pattern.compile("^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$"));


    public static final ValidationKeys<TextValueObjectValidationException.Reason> CODE_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.attribute_definitions.code.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.attribute_definitions.code.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.attribute_definitions.code.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.attribute_definitions.code.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.attribute_definitions.code.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.attribute_definitions.code.invalid"

            ));


    public static final String KEY_NOT_ALLOWED_FOR_TYPE =
            "validation.attribute_definitions.numericConstraints.not_allowed_for_type";
    public static final String KEY_TYPE_NULL =
            "validation.attribute_definitions.type.is_null";
    public static final String KEY_UNIT_INACTIVE =
            "validation.attribute_definitions.unit.is_inactive";
    public static final String KEY_DECIMAL_SCALE_REQUIRED_FOR_DECIMAL =
            "validation.attribute_definitions.decimalScale.required_for_decimal";
    public static final String KEY_DECIMAL_SCALE_NOT_ALLOWED_FOR_INT =
            "validation.attribute_definitions.decimalScale.not_allowed_for_int";
    public static final String KEY_TRANSLATION_ALREADY_EXISTS =
            "validation.attribute_definitions.translation.already_exists";
    public static final String KEY_TRANSLATION_NOT_EXISTS =
            "validation.attribute_definitions.translation.not_exists";
    public static final String KEY_LANGUAGE_CODE_NULL =
            "validation.attribute_definitions.languageCode.is_null";
    public static final String KEY_ATTRIBUTE_OPTION_CODE_NULL =
            "validation.attribute_definitions.attribute_.is_null";
    public static final String KEY_OPTION_ALREADY_EXISTS =
            "validation.attribute_definitions.attributeOption.already_exists";

    public static final String KEY_OPTION_NOT_EXISTS =
            "validation.attribute_definitions.attributeOption.not_exists";
}
