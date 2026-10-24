package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.price.PriceValueObjectValidationException;
import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.regex.Pattern;

public final class OrderItemValidation {

    private OrderItemValidation() {}

    public static final String KEY_ORDER_IS_NULL = "validation.order_items.order.is_null";
    public static final String KEY_PRODUCT_IS_NULL = "validation.order_items.product.is_null";
    public static final String KEY_QUANTITY_INVALID = "validation.order_items.quantity_invalid";
    public static final String KEY_UNIT_PRICE_INVALID = "validation.order_items.price.invalid";
    public static final String KEY_SKU_INVALID = "validation.order_items.skuSnapshot.invalid";
    public static final String KEY_PRODUCT_NAME_INVALID ="validation.order_items.productNameSnapshot.invalid";

    public static final ValidationKeys<PriceValueObjectValidationException.Reason> UNIT_PRICE_KEYS =
            new ValidationKeys<>(
                    Map.of(
                            PriceValueObjectValidationException.Reason.NULL,
                            "validation.order_items.unitPrice.is_null",

                            PriceValueObjectValidationException.Reason.NEGATIVE_NOT_ALLOWED,
                            "validation.order_items.unitPrice.negative_not_allowed",

                            PriceValueObjectValidationException.Reason.INVALID_SCALE,
                            "validation.order_items.unitPrice.invalid_scale",

                            PriceValueObjectValidationException.Reason.TOO_SMALL,
                            "validation.order_items.unitPrice.too_small",

                            PriceValueObjectValidationException.Reason.TOO_LARGE,
                            "validation.order_items.unitPrice.too_large"
                    )
            );



    public static final TextSpec PRODUCT_NAME_SPEC = TextSpec.length(1, 300);

    public static final ValidationKeys<TextValueObjectValidationException.Reason> PRODUCT_NAME_KEYS =
            new ValidationKeys<>(
                    Map.of(
                            TextValueObjectValidationException.Reason.NULL,
                            "validation.order_items.productName.is_null",

                            TextValueObjectValidationException.Reason.EMPTY,
                            "validation.order_items.productName.is_empty",

                            TextValueObjectValidationException.Reason.TOO_SHORT,
                            "validation.order_items.productName.too_short",

                            TextValueObjectValidationException.Reason.TOO_LONG,
                            "validation.order_items.productName.too_long",

                            TextValueObjectValidationException.Reason.INVALID_LENGTH,
                            "validation.order_items.productName.invalid_length",

                            TextValueObjectValidationException.Reason.INVALID_FORMAT,
                            "validation.order_items.productName.invalid"
                    )
            );


    public static final TextSpec SKU_SPEC = TextSpec.pattern(8, 12, Pattern.compile("^(?:[A-Z]{1,3}[0-9]+|[0-9]+)$"));
    public static final ValidationKeys<TextValueObjectValidationException.Reason> SKU_KEYS =
            new ValidationKeys<>(
                    Map.of(
                            TextValueObjectValidationException.Reason.NULL,
                            "validation.order_items.sku.is_null",

                            TextValueObjectValidationException.Reason.EMPTY,
                            "validation.order_items.sku.is_empty",

                            TextValueObjectValidationException.Reason.TOO_SHORT,
                            "validation.order_items.sku.too_short",

                            TextValueObjectValidationException.Reason.TOO_LONG,
                            "validation.order_items.sku.too_long",

                            TextValueObjectValidationException.Reason.INVALID_LENGTH,
                            "validation.order_items.sku.invalid_length",

                            TextValueObjectValidationException.Reason.INVALID_FORMAT,
                            "validation.order_items.sku.invalid"
                    )
            );
}
