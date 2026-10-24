package org.example.amaizon.model.validators.text;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;


import java.util.Locale;
import java.util.function.UnaryOperator;

public final class TextValidator {

    private TextValidator() {}

    public static String validate(String raw, TextSpec spec, UnaryOperator<String> normalizer) {
        if (raw == null)
            ex(TextValueObjectValidationException.Reason.NULL);

        String trimmed = raw.trim();
        if (trimmed.isEmpty())
            ex(TextValueObjectValidationException.Reason.EMPTY);

        String normalized = (normalizer != null) ? normalizer.apply(trimmed) : trimmed;

        int length = normalized.length();

        if (spec.min() == spec.max() && length != spec.max())
            ex(TextValueObjectValidationException.Reason.INVALID_LENGTH, spec.max());

        if (length < spec.min())
            ex(TextValueObjectValidationException.Reason.TOO_SHORT, spec.min());

        if (length > spec.max())
            ex(TextValueObjectValidationException.Reason.TOO_LONG, spec.max());

        if (spec.pattern() != null && !spec.pattern().matcher(normalized).matches())
            ex(TextValueObjectValidationException.Reason.INVALID_FORMAT);

        return normalized;
    }

    public static UnaryOperator<String> lowerRoot() {
        return s -> s.toLowerCase(Locale.ROOT);
    }

    public static UnaryOperator<String> upperRoot(){ return s -> s.toUpperCase(Locale.ROOT);}

    public static UnaryOperator<String> slugNormalizer() {
        return raw -> {
            String lower = raw.toLowerCase(Locale.ROOT);

            // usuń polskie znaki
            String normalized = java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");

            // spacje na -
            normalized = normalized.replaceAll("\\s+", "-");

            // usuń wszystko poza a-z0-9-
            normalized = normalized.replaceAll("[^a-z0-9-]", "");

            // usuń wielokrotne -
            normalized = normalized.replaceAll("-{2,}", "-");

            // usuń - z początku i końca
            normalized = normalized.replaceAll("^-|-$", "");

            return normalized;
        };
    }

    public static UnaryOperator<String> globalProductCode() {
        return raw -> raw
                .replaceAll("\\s+", "")     // usuń spacje
                .toUpperCase(Locale.ROOT);  // zawsze uppercase
    }



    private static void ex(TextValueObjectValidationException.Reason reason, Object... args) {
        throw new TextValueObjectValidationException(reason, args);
    }
}
