package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.regex.Pattern;

public class CurrencyValidation {


    public static final TextSpec CODE_SPEC =
            TextSpec.fixedWithPattern(3, Pattern.compile("^[A-Z]{3}$"));

    public static final ValidationKeys<TextValueObjectValidationException.Reason> CODE_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.currency.code.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.currency.code.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.currency.code.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.currency.code.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.currency.code.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.currency.code.invalid"
            )
    );

    public static final TextSpec SYMBOL_SPEC = TextSpec.pattern(1, 5, Pattern.compile("^(?:\\p{Sc}|\\p{L}{2,5}|\\p{L}{1,3}\\p{Sc})$"));

    public static final ValidationKeys<TextValueObjectValidationException.Reason> SYMBOL_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.currency.symbol.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.currency.symbol.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.currency.symbol.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.currency.symbol.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.currency.symbol.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.currency.symbol.invalid"
            )
    );


    public static final TextSpec NAME_SPEC =
            TextSpec.pattern(1, 50, Pattern.compile("^[A-Za-z ]+$"));

    public static final ValidationKeys<TextValueObjectValidationException.Reason> NAME_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.currency.name.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.currency.name.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.currency.name.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.currency.name.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.currency.name.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.currency.name.invalid"
            )
    );
}
