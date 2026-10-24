package org.example.amaizon.model.embeddable.attributeDefinition;

import jakarta.persistence.Embeddable;
import org.example.amaizon.exceptions.validation.ProductAttributeValueValidationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Embeddable
public class AttributeValue {

    public static final int VALUE_STRING_MAX_LENGTH = 500;
    public static final int VALUE_CODE_MAX_LENGTH = 100;

    public static final int VALUE_NUMBER_MAX_PRECISION = 16;
    public static final int VALUE_NUMBER_MAX_SCALE = 2;

    private static final String KEY_VALUE_NULL =
            "validation.product_attribute_values.value.not_set";
    private static final String KEY_VALUE_MULTIPLE_SET =
            "validation.product_attribute_values.value.multiple_set";

    private static final String KEY_STRING_NULL =
            "validation.product_attribute_values.valueString.is_null";
    private static final String KEY_STRING_EMPTY =
            "validation.product_attribute_values.valueString.is_empty";
    private static final String KEY_STRING_TOO_LONG =
            "validation.product_attribute_values.valueString.too_long";

    private static final String KEY_CODE_NULL =
            "validation.product_attribute_values.valueCode.is_null";
    private static final String KEY_CODE_EMPTY =
            "validation.product_attribute_values.valueCode.is_empty";
    private static final String KEY_CODE_TOO_LONG =
            "validation.product_attribute_values.valueCode.too_long";

    private static final String KEY_NUMBER_NULL =
            "validation.product_attribute_values.valueNumber.is_null";
    private static final String KEY_NUMBER_TOO_PRECISE =
            "validation.product_attribute_values.valueNumber.too_precise";
    private static final String KEY_NUMBER_TOO_MANY_DEC =
            "validation.product_attribute_values.valueNumber.too_many_decimals";

    private static final String KEY_BOOLEAN_NULL =
            "validation.product_attribute_values.valueBoolean.is_null";
    private static final String KEY_TIMESTAMP_NULL =
            "validation.product_attribute_values.valueTimestamp.is_null";



    private String valueString;

    private BigDecimal valueNumber;

    private Boolean valueBoolean;

    private Instant valueTimestamp;

    private String valueCode;

    protected AttributeValue() {
    }



    public static AttributeValue ofString(String raw) {
        requireNonNull(raw, KEY_STRING_NULL);

        String trimmed = raw.trim();
        requireNonemptyAfterTrim(trimmed, KEY_STRING_EMPTY);
        requireMaxLength(trimmed, VALUE_STRING_MAX_LENGTH, KEY_STRING_TOO_LONG);

        AttributeValue av = new AttributeValue();
        av.valueString = trimmed;
        return av;
    }

    public static AttributeValue ofCode(String raw) {
        requireNonNull(raw, KEY_CODE_NULL);

        String trimmed = raw.trim();
        requireNonemptyAfterTrim(trimmed, KEY_CODE_EMPTY);
        requireMaxLength(trimmed, VALUE_CODE_MAX_LENGTH, KEY_CODE_TOO_LONG);

        AttributeValue av = new AttributeValue();
        av.valueCode = trimmed;
        return av;
    }

    public static AttributeValue ofNumber(BigDecimal number) {
        requireNonNull(number, KEY_NUMBER_NULL);
        requirePrecisionAndScale(number);

        AttributeValue av = new AttributeValue();
        av.valueNumber = number;
        return av;
    }

    public static AttributeValue ofBoolean(Boolean value) {
        requireNonNull(value, KEY_BOOLEAN_NULL);

        AttributeValue av = new AttributeValue();
        av.valueBoolean = value;
        return av;
    }

    public static AttributeValue ofTimestamp(Instant value) {
        requireNonNull(value, KEY_TIMESTAMP_NULL);

        AttributeValue av = new AttributeValue();
        av.valueTimestamp = value;
        return av;
    }



    public void validateOrThrow() {
        int count = 0;
        if (valueString != null) count++;
        if (valueNumber != null) count++;
        if (valueBoolean != null) count++;
        if (valueTimestamp != null) count++;
        if (valueCode != null) count++;

        if (count == 0) ex(KEY_VALUE_NULL);
        if (count > 1) ex(KEY_VALUE_MULTIPLE_SET);

        if (valueString != null) {
            String t = valueString.trim();
            requireNonemptyAfterTrim(t, KEY_STRING_EMPTY);
            requireMaxLength(t, VALUE_STRING_MAX_LENGTH, KEY_STRING_TOO_LONG);
            valueString = t;
        }

        if (valueCode != null) {
            String t = valueCode.trim();
            requireNonemptyAfterTrim(t, KEY_CODE_EMPTY);
            requireMaxLength(t, VALUE_CODE_MAX_LENGTH, KEY_CODE_TOO_LONG);
            valueCode = t;
        }

        if (valueNumber != null) {
            requirePrecisionAndScale(valueNumber);
        }
    }


    private static void requireNonNull(Object value, String key) {
        if (value == null) ex(key);
    }

    private static void requireNonemptyAfterTrim(String value, String key) {
        if (value.isEmpty()) ex(key);
    }

    private static void requireMaxLength(String value, int maxLength, String key) {
        if (value.length() > maxLength) ex(key, maxLength);
    }

    private static void requirePrecisionAndScale(BigDecimal number) {
        int scale = Math.max(number.scale(), 0);
        if (scale > VALUE_NUMBER_MAX_SCALE) {
            ex(KEY_NUMBER_TOO_MANY_DEC);
        }

        int precision = number.precision();
        int integerDigits = precision - scale;
        int maxIntegerDigits = VALUE_NUMBER_MAX_PRECISION - VALUE_NUMBER_MAX_SCALE;

        if (integerDigits > maxIntegerDigits) {
            ex(KEY_NUMBER_TOO_PRECISE);
        }
    }

    private static void ex(String key) {
        throw new ProductAttributeValueValidationException(key);
    }

    private static void ex(String key, Object... args) {
        throw new ProductAttributeValueValidationException(key, args);
    }

    public String getValueString() { return valueString; }
    public BigDecimal getValueNumber() { return valueNumber; }
    public Boolean getValueBoolean() { return valueBoolean; }
    public Instant getValueTimestamp() { return valueTimestamp; }
    public String getValueCode() { return valueCode; }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttributeValue other)) return false;
        return Objects.equals(valueString, other.valueString)
                && Objects.equals(valueNumber, other.valueNumber)
                && Objects.equals(valueBoolean, other.valueBoolean)
                && Objects.equals(valueTimestamp, other.valueTimestamp)
                && Objects.equals(valueCode, other.valueCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueString, valueNumber, valueBoolean, valueTimestamp, valueCode);
    }

    @Override
    public String toString() {
        if (valueString != null) return valueString;
        if (valueCode != null) return valueCode;
        if (valueNumber != null) return valueNumber.toPlainString();
        if (valueBoolean != null) return valueBoolean.toString();
        if (valueTimestamp != null) return valueTimestamp.toString();
        return "";
    }

}
