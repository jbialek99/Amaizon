package org.example.amaizon.model.embeddable.all;

import jakarta.persistence.Embeddable;
import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextFactory;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Objects;
import java.util.function.BiConsumer;

@Embeddable
public class LanguageCode {


    private String value;

    protected LanguageCode() {
    }

    private LanguageCode(String value) {
        this.value = value;
    }

    public static LanguageCode of(String raw,
                                  TextSpec spec,
                                  ValidationKeys<TextValueObjectValidationException.Reason> keys,
                                  BiConsumer<String, Object[]> throwWithKey) {
        return TextFactory.createOrThrow(
                raw,
                spec,
                keys,
                throwWithKey,
                null,
                LanguageCode::new
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
        if (!(o instanceof LanguageCode other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
