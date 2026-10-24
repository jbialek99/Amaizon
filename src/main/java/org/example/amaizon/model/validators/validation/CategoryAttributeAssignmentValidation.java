package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.intNumber.IntValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.intNumber.IntSpec;

import java.util.Map;

public class CategoryAttributeAssignmentValidation {

    public static final String KEY_CATEGORY_NULL = "validation.category_attribute_assignments.category.is_null";
    public static final String KEY_ATTRIBUTE_DEFINITION_NULL = "validation.category_attribute_assignments.attribute_definition.is_null";
    public static final String KEY_CATEGORY_MISMATCH = "validation.category_attribute_assignments.category.mismatch";

    public static final IntSpec POSITION_SPEC = new IntSpec(1,1000);

    public static final ValidationKeys<IntValueObjectValidationException.Reason> POSITION_KEYS = new ValidationKeys<>(
            Map.of(
                    IntValueObjectValidationException.Reason.NULL,
                    "validation.category_attribute_assignments.position.is_null",

                    IntValueObjectValidationException.Reason.TOO_SMALL,
                    "validation.category_attribute_assignments.position.too_small",

                    IntValueObjectValidationException.Reason.TOO_LARGE,
                    "validation.category_attribute_assignments.position.too_large"
            )
    );
}
