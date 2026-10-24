package org.example.amaizon.model.validators.validation;


import org.example.amaizon.exceptions.intNumber.IntValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.intNumber.IntSpec;

import java.util.Map;

public class CategoryAttributeGroupValidation {

    public static final String KEY_CATEGORY_NULL =
            "validation.category_attribute_groups.category.is_null";

    public static final String KEY_ATTRIBUTE_GROUP_NULL =
            "validation.category_attribute_groups.attributeGroup.is_null";

    public static final String KEY_CATEGORY_NOT_FOUND =
            "validation.category_attribute_groups.category.not_exists";

    public static final IntSpec POSITION_SPEC = new IntSpec(1,1000);

    public static final ValidationKeys<IntValueObjectValidationException.Reason> POSITION_KEYS =
            new ValidationKeys<>(
                    Map.of(
                            IntValueObjectValidationException.Reason.NULL,
                            "validation.category_attribute_groups.position.is_null",

                            IntValueObjectValidationException.Reason.TOO_SMALL,
                            "validation.category_attribute_groups.position.too_small",

                            IntValueObjectValidationException.Reason.TOO_LARGE,
                            "validation.category_attribute_groups.position.too_large"
                    )
            );
}
