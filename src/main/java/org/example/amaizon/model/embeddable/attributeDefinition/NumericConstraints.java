package org.example.amaizon.model.embeddable.attributeDefinition;

import jakarta.persistence.Embeddable;
import org.example.amaizon.exceptions.validation.AttributeDefinitionValidationException;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class NumericConstraints {

    private static final String KEY_SCALE_NEGATIVE =
            "validation.attribute_definitions.decimal_scale.negative";
    private static final String KEY_MIN_SCALE_TOO_LARGE =
            "validation.attribute_definitions.minimum.scale_too_large";
    private static final String KEY_MAX_SCALE_TOO_LARGE =
            "validation.attribute_definitions.maximum.scale_too_large";
    private static final String KEY_MIN_GREATER_THAN_MAX =
            "validation.attribute_definitions.minimum_greater_than_maximum";


    private BigDecimal minimum;

    private BigDecimal maximum;

    private Integer decimalScale;

    protected NumericConstraints() {}


    private NumericConstraints(BigDecimal minimum, BigDecimal maximum, Integer decimalScale) {
        this.decimalScale = decimalScale;
        this.minimum = minimum;
        this.maximum = maximum;
        validate();
    }

    public static NumericConstraints of(BigDecimal minimum, BigDecimal maximum, Integer decimalScale) {
        return new NumericConstraints(minimum, maximum, decimalScale);
    }

    public BigDecimal getMinimum() { return minimum; }
    public BigDecimal getMaximum() { return maximum; }
    public Integer getDecimalScale() { return decimalScale; }

    public boolean isDefined() {
        return minimum != null || maximum != null || decimalScale != null;
    }

    private void validate() {
        if (decimalScale != null && decimalScale < 0) ex(KEY_SCALE_NEGATIVE);

        if (minimum != null) validateScale(minimum, KEY_MIN_SCALE_TOO_LARGE);
        if (maximum != null) validateScale(maximum, KEY_MAX_SCALE_TOO_LARGE);

        if (minimum != null && maximum != null && minimum.compareTo(maximum) > 0) {
            ex(KEY_MIN_GREATER_THAN_MAX);
        }
    }

    private void validateScale(BigDecimal value, String messageKey) {
        if (decimalScale == null) return;

        int valueScale = Math.max(value.scale(), 0);
        if (valueScale > decimalScale) {
            ex(messageKey, decimalScale, valueScale);
        }
    }

    private static void ex(String key, Object... args) {
        throw new AttributeDefinitionValidationException(key, args);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumericConstraints other)) return false;
        return Objects.equals(minimum, other.minimum)
                && Objects.equals(maximum, other.maximum)
                && Objects.equals(decimalScale, other.decimalScale);
    }

    @Override public int hashCode() {
        return Objects.hash(minimum, maximum, decimalScale);
    }

    @Override
    public String toString() {
        if (!isDefined()) return "NumericConstraints[undefined]";

        return "NumericConstraints[" +
                "min=" + minimum +
                ", max=" + maximum +
                ", scale=" + decimalScale +
                "]";
    }

}
