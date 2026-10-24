package org.example.amaizon.model.embeddable.all;

import jakarta.persistence.Embeddable;
import org.example.amaizon.exceptions.imageUrl.ImageUrlValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.imageUrl.ImageUrlFactory;
import org.example.amaizon.model.validators.imageUrl.ImageUrlSpec;
import org.example.amaizon.model.validators.imageUrl.ImageUrlValidator;

import java.util.Objects;
import java.util.function.BiConsumer;

@Embeddable
public class ImageUrl {

    private String value;

    private ImageUrl(String value) {
        this.value = value;
    }

    protected ImageUrl() {}

    public static  ImageUrl of(String raw,
                               ImageUrlSpec spec,
                               ValidationKeys<ImageUrlValueObjectValidationException.Reason> keys,
                               BiConsumer<String, Object[]> throwWithKey) {

        return ImageUrlFactory.createOrThrow(
                raw,
                spec,
                keys,
                throwWithKey,
                ImageUrlValidator.defaultNormalize(),
                ImageUrl::new
        );
    }
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageUrl other)) return false;
        return Objects.equals(value, other.value);
    }


    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
