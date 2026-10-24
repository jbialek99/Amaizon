package org.example.amaizon.model.validators.imageUrl;

import org.example.amaizon.exceptions.imageUrl.ImageUrlValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;


public class ImageUrlFactory {

    private ImageUrlFactory() {
    }

    public static <T> T createOrThrow(
            String raw,
            ImageUrlSpec spec,
            ValidationKeys<ImageUrlValueObjectValidationException.Reason> keys,
            BiConsumer<String, Object[]> throwWithKey,
            UnaryOperator<String> normalize,
            Function<String, T> constructor

    ) {
        try {
            String normalized = ImageUrlValidator.validate(raw, spec, normalize);
            return constructor.apply(normalized);

        } catch (ImageUrlValueObjectValidationException e) {
            String message = keys.resolve(e.getReason());
            throwWithKey.accept(message, e.getArgs());
            throw new IllegalStateException("throwWithKey did not throw");


        }
    }

}
