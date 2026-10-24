package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.imageUrl.ImageUrlValueObjectValidationException;
import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.imageUrl.ImageUrlSpec;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ProductValidation {

    public static final String KEY_CATEGORY_IS_NULL = "validation.product.category.is_null";
    public static final String KEY_CATEGORY_ALREADY_EXISTS = "validation.product.category.already_exists";
    public static final String KEY_CATEGORY_NOT_DEFINED = "validation.product.category.not_defined";
    public static final String KEY_ACTIVATE_WITHOUT_CATEGORY = "validation.product.activate_without_category";
    public static final String KEY_COUNTRY_ALREADY_ASSIGNED = "validation.product.country.already_assigned";
    public static final String KEY_STATUS_IS_NULL = "validation.product.status.is_null";
    public static final String KEY_COUNTRY_IS_NULL = "validation.product.country.is_null";
    public static final String KEY_COUNTRY_NOT_ASSIGNED = "validation.product.country.not_assigned";



    public static final TextSpec BRAND_SPEC = TextSpec.max(150);

    public static final ValidationKeys<TextValueObjectValidationException.Reason> BRAND_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.product.brand.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.product.brand.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.product.brand.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.product.brand.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.product.brand.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.product.brand.invalid"
            )
    );


    public static final TextSpec SKU_SPEC = TextSpec.pattern(8, 12, Pattern.compile("^(?:[A-Z]{1,3}[0-9]+|[0-9]+)$"));

    public static final ValidationKeys<TextValueObjectValidationException.Reason> SKU_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.product.sku.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.product.sku.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.product.sku.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.product.sku.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.product.sku.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.product.sku.invalid"
            )
    );




   public static final  ImageUrlSpec IMAGE_URL_SPEC = ImageUrlSpec.httpsExactHost(
            10,
            300,
            Set.of("i.imgur.com"),
            Set.of("jpg", "jpeg", "png", "webp")
    );


    public static final ValidationKeys<ImageUrlValueObjectValidationException.Reason> IMAGE_URL_KEYS = new ValidationKeys<>(
            Map.of(
                    ImageUrlValueObjectValidationException.Reason.NULL,
                    "validation.product.imageUrl.is_null",

                    ImageUrlValueObjectValidationException.Reason.EMPTY,
                    "validation.product.imageUrl.is_empty",

                    ImageUrlValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.product.imageUrl.too_short",

                    ImageUrlValueObjectValidationException.Reason.TOO_LONG,
                    "validation.product.imageUrl.too_long",

                    ImageUrlValueObjectValidationException.Reason.INVALID_URI,
                    "validation.product.imageUrl.invalid_URI",

                    ImageUrlValueObjectValidationException.Reason.INVALID_SCHEME,
                    "validation.product.imageUrl.invalid_scheme",

                    ImageUrlValueObjectValidationException.Reason.HOST_NOT_ALLOWED,
                    "validation.product.imageUrl.host_not_allowed",

                    ImageUrlValueObjectValidationException.Reason.QUERY_NOT_ALLOWED,
                    "validation.product.imageUrl.invalid.query_not_allowed",

                    ImageUrlValueObjectValidationException.Reason.FRAGMENT_NOT_ALLOWED,
                    "validation.product.imageUrl.invalid.fragment_not_allowed",

                    ImageUrlValueObjectValidationException.Reason. INVALID_PATH,
                    "validation.product.imageUrl.invalid_path"


            )
    );

}
