package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.imageUrl.ImageUrlValueObjectValidationException;
import org.example.amaizon.exceptions.intNumber.IntValueObjectValidationException;
import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.imageUrl.ImageUrlSpec;
import org.example.amaizon.model.validators.intNumber.IntSpec;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class CategoryValidation {

    public static final String KEY_COUNTRY_ALREADY_ASSIGN = "validation.category.country_already_assigned";
    public static final String KEY_STATUS_NULL = "validation.category.status.is_null";
    public static final String KEY_CATEGORY_IS_NULL = "validation.category.is_null";
    public static final String KEY_COUNTRY_IS_NULL = "validation.category.country.is_null";
    public static final String KEY_COUNTRY_NOT_ASSIGNED = "validation.category.country.not_assigned";
    public static final String  KEY_PARENT_CANNOT_BE_SELF = "validation.category.parent.cannot_be_self";
    public static final String KEY_PARENT_CANNOT_BE_CHILD = "validation.category.parent.cannot_be_child";
    public static final String KEY_ATTRIBUTE_DEFINITION_NULL = "validation.category.attributeDefinition.null";
    public static final String KEY_DEFINITION_ALREADY_EXIST = "validation.category.attributeDefinition.already_exist";
    public static final String KEY_GROUP_BELONGS_ANOTHER_CATEGORY = "validation.category.group.belongs_another_category";

    public static final TextSpec NAME_SPEC =
            TextSpec.pattern(1, 100, Pattern.compile("^[\\p{L}\\p{N} &-]+$"));

    public static ValidationKeys<TextValueObjectValidationException.Reason> NAME_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.category.name.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.category.name.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.category.name.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.category.name.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.category.name.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.category.name.invalid"

            ));

    public static final ImageUrlSpec IMAGE_URL_SPEC = ImageUrlSpec.httpsExactHost(
            10,
            300,
            Set.of("i.imgur.com"),
            Set.of("jpg", "jpeg", "png", "webp")
    );


    public static final ValidationKeys<ImageUrlValueObjectValidationException.Reason> IMAGE_URL_KEYS = new ValidationKeys<>(
            Map.of(
                    ImageUrlValueObjectValidationException.Reason.NULL,
                    "validation.category.imageUrl.is_null",

                    ImageUrlValueObjectValidationException.Reason.EMPTY,
                    "validation.category.imageUrl.is_empty",

                    ImageUrlValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.category.imageUrl.too_short",

                    ImageUrlValueObjectValidationException.Reason.TOO_LONG,
                    "validation.category.imageUrl.too_long",

                    ImageUrlValueObjectValidationException.Reason.INVALID_URI,
                    "validation.category.imageUrl.invalid_URI",

                    ImageUrlValueObjectValidationException.Reason.INVALID_SCHEME,
                    "validation.category.imageUrl.invalid_scheme",

                    ImageUrlValueObjectValidationException.Reason.HOST_NOT_ALLOWED,
                    "validation.category.imageUrl.host_not_allowed",

                    ImageUrlValueObjectValidationException.Reason.QUERY_NOT_ALLOWED,
                    "validation.category.imageUrl.invalid.query_not_allowed",

                    ImageUrlValueObjectValidationException.Reason.FRAGMENT_NOT_ALLOWED,
                    "validation.category.imageUrl.invalid.fragment_not_allowed",

                    ImageUrlValueObjectValidationException.Reason.INVALID_PATH,
                    "validation.category.imageUrl.invalid_path"


            )
    );


    public static final IntSpec DISPLAY_ORDER_SPEC = new IntSpec(1, 1000);

    public static final ValidationKeys<IntValueObjectValidationException.Reason> DISPLAY_ORDER_KEYS = new ValidationKeys<>(
            Map.of(
                    IntValueObjectValidationException.Reason.NULL,
                    "validation.category.displayOrder.is_null",

                    IntValueObjectValidationException.Reason.TOO_SMALL,
                    "validation.category.displayOrder.too_small",

                    IntValueObjectValidationException.Reason.TOO_LARGE,
                    "validation.category.displayOrder.too_large"
            )
    );
}
