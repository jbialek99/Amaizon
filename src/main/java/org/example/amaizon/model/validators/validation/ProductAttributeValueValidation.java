package org.example.amaizon.model.validators.validation;

public class ProductAttributeValueValidation {

    public static final String KEY_PRODUCT_NULL = "validation.product_attribute_values.product.is_null";
    public static final String KEY_ATTRIBUTE_DEFINITION_NULL = "validation.product_attribute_values.attribute_definitions.is_null";
    public static final String KEY_POSITION_NEGATIVE = "validation.product_attribute_values.position.negative";
    public static final String KEY_POSITION_NOT_ALLOWED = "validation.product_attribute_values.position.not_allowed_for_single_value";
    public static final String KEY_VALUE_TYPE_MISMATCH = "validation.product_attribute_values.value.type_mismatch";

    public static final String VALUE_NUMBER_TOO_LOW ="validation.product_attribute_values.valueNumber.too_low";
    public static final String VALUE_NUMBER_TOO_HIGH ="validation.product_attribute_values.valueNumber.too_high";
    public static final String VALUE_NOT_SET = "validation.product_attribute_values.value.not_set";
}
