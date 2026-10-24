package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.intNumber.IntValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.intNumber.IntSpec;

import java.util.Map;

public class AttributeGroupItemValidation {
    public static final IntSpec DISPLAY_SPEC = IntSpec.range(1, 1000);

    public static final ValidationKeys<IntValueObjectValidationException.Reason> DISPLAY_KEYS = new ValidationKeys<>(
            Map.of(
                    IntValueObjectValidationException.Reason.NULL,
                    "validation.attribute_group_items.displayOrder.is_null",
                    IntValueObjectValidationException.Reason.TOO_SMALL,
                    "validation.attribute_group_items.displayOrder.too_small",
                    IntValueObjectValidationException.Reason.TOO_LARGE,
                    "validation.attribute_group_items.displayOrder.too_large"
            ));
    public static final String KEY_ATTRIBUTE_GROUP_NULL = "validation.attribute_group_items.attributeGroup.is_null";
    public static final String KEY_ATTRIBUTE_DEFINITION_NULL = "validation.attribute_group_items.attributeDefinition.is_null";



}
