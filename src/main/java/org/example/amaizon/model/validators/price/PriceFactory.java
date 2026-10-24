package org.example.amaizon.model.validators.price;



import org.example.amaizon.exceptions.price.PriceValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;

import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class PriceFactory {

    private PriceFactory() {}

    public static <T> T createOrThrow(
            BigDecimal raw,
            ValidationKeys<PriceValueObjectValidationException.Reason> keys,
            BiConsumer<String, Object[]> throwWithKey,
            Function<BigDecimal, T> constructor
    ) {
        try {
            BigDecimal normalized = PriceValidator.validate(raw);
            return constructor.apply(normalized);
        } catch (PriceValueObjectValidationException e) {
            String messageKey = keys.resolve(e.getReason());
            throwWithKey.accept(messageKey, e.getArgs());
            throw new IllegalStateException("throwWithKey did not throw");
        }
    }
}
