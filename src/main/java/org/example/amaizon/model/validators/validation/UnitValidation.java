package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;


public class UnitValidation {


    public static final String KEY_DIMENSION_IS_NULL = "validation.unit.dimension.is_null";

    public static final TextSpec CODE_SPEC = TextSpec.max(20);

    public static final ValidationKeys<TextValueObjectValidationException.Reason> CODE_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.unit.code.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.unit.code.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.unit.code.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.unit.code.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.unit.code.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.unit.code.invalid"
            )
    );


    public static TextSpec SYMBOL_SPEC = TextSpec.max(20);

    public static ValidationKeys<TextValueObjectValidationException.Reason> SYMBOL_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.unit.symbol.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.unit.symbol.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.unit.symbol.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.unit.symbol.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.unit.symbol.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.unit.symbol.invalid"

            )
    );
}
