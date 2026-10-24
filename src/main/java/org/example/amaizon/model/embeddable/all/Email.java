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
public class Email {

    private String value;

    private Email(String value){
        this.value = value;
    }

    protected Email() {
    }

    public static  Email of(String email, TextSpec spec, ValidationKeys<TextValueObjectValidationException.Reason> keys, BiConsumer<String, Object[]> throwWithKey) {
    return TextFactory.createOrThrow(
            email,
            spec,
            keys,
            throwWithKey,
            TextValidator.lowerRoot(),
            Email::new
    );


    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Email other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
