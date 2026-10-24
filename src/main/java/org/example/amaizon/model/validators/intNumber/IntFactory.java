package org.example.amaizon.model.validators.intNumber;


import org.example.amaizon.exceptions.intNumber.IntValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class IntFactory {

    private IntFactory() {}

    public static <T> T createOrThrow(
            Integer raw,
            IntSpec spec,
            ValidationKeys<IntValueObjectValidationException.Reason> keys,
            BiConsumer<String, Object[]> throwWithKey,
            Function<Integer, T> constructor
    ) {
        try {
            Integer validated = IntValidator.validate(raw, spec);
            return constructor.apply(validated);

        } catch (IntValueObjectValidationException e) {
            String key = keys.resolve(e.getReason());
            throwWithKey.accept(key, e.getArgs());
            throw new IllegalStateException("throwWithKey did not throw");
        }
    }

}
