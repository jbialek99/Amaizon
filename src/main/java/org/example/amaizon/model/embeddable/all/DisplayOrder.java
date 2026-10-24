package org.example.amaizon.model.embeddable.all;


import jakarta.persistence.Embeddable;
import org.example.amaizon.exceptions.intNumber.IntValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.intNumber.IntFactory;
import org.example.amaizon.model.validators.intNumber.IntSpec;

import java.util.Objects;
import java.util.function.BiConsumer;

@Embeddable
public class DisplayOrder {

    private Integer value;


    private DisplayOrder(Integer value) {
        this.value = value;
    }
    protected DisplayOrder() {
    }

    public static DisplayOrder of(
            Integer raw,
            IntSpec spec,
            ValidationKeys<IntValueObjectValidationException.Reason> keys,
            BiConsumer<String, Object[]> throwWithKey
    ) {
        return IntFactory.createOrThrow(
                raw,
                spec,
                keys,
                throwWithKey,
                DisplayOrder::new
        );
    }





    public Integer getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DisplayOrder other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
