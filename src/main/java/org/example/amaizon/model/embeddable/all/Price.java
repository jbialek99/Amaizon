package org.example.amaizon.model.embeddable.all;

import jakarta.persistence.Embeddable;
import org.example.amaizon.exceptions.price.PriceValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.price.PriceFactory;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.BiConsumer;

@Embeddable
public class Price {

    private BigDecimal value;

    private Price(BigDecimal value) {
        this.value = value;
    }

    protected Price() { }


    public static Price of(
            BigDecimal raw,
            ValidationKeys<PriceValueObjectValidationException.Reason> keys,
            BiConsumer<String, Object[]> throwWithKey
    ) {
        return PriceFactory.createOrThrow(
                raw,
                keys,
                throwWithKey,
                Price::new
        );
    }


    public BigDecimal getValue() {
        return value;
    }

    @Override public String toString() {
        return value.toPlainString();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Price other)) return false;
        return value != null && other.value != null && value.compareTo(other.value) == 0;
    }

    @Override public int hashCode() {
        return Objects.hash(value == null ? null : value.stripTrailingZeros());
    }
}
