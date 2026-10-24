package org.example.amaizon.model.embeddable.all;

import jakarta.persistence.Embeddable;
import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextFactory;
import org.example.amaizon.model.validators.text.TextSpec;
import org.example.amaizon.model.validators.text.TextValidator;

import java.util.Objects;
import java.util.function.BiConsumer;

@Embeddable
public class Slug {

    private String value;

    private Slug(String value) {
        this.value = value;
    }

    protected Slug() {}

    public static Slug of(String value,
                          TextSpec spec,
                          ValidationKeys<TextValueObjectValidationException.Reason> keys,
                          BiConsumer<String, Object[]> throwWithKey) {
        return TextFactory.createOrThrow(
                value,
                spec,
                keys,
                throwWithKey,
                TextValidator.slugNormalizer(),
                Slug::new
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
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Slug other)) return false;
        return Objects.equals(value, other.value);
    }
}
