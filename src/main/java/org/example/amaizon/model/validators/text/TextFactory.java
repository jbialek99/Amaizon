package org.example.amaizon.model.validators.text;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;


import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class TextFactory {

    private TextFactory() {}

    public static <T> T createOrThrow(
            String raw,
            TextSpec spec,
            ValidationKeys<TextValueObjectValidationException.Reason> keys,
            BiConsumer<String, Object[]> throwWithKey,
            UnaryOperator<String> normalizer,
            Function<String, T> constructor
    ) {
        try {
            String validated = TextValidator.validate(raw, spec, normalizer);
            return constructor.apply(validated);

        } catch (TextValueObjectValidationException e) {
            String key = keys.resolve(e.getReason());
            throwWithKey.accept(key, e.getArgs());
            throw new IllegalStateException("throwWithKey did not throw");
        }
    }


}
