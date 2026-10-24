package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.price.PriceValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;

import java.util.Map;

public class OrderValidation {


    public static final String KEY_COUNTRY_IS_NULL = "validation.order.country.is_null";
    public static final String KEY_CURRENCY_IS_NULL = "validation.order.currency.is_null";
    public static final String KEY_ORDER_LOCKED = "validation.order.is_locked";
    public static final String KEY_ITEM_IS_NULL = "validation.order.user.item.is_null";
    public static final String KEY_ITEM_NOT_FOUND = "validation.order.item.not_found";
    public static final String KEY_PAYMENT_ALREADY_EXISTS = "validation.order.payment.already_exists";
    public static final String KEY_TOTAL_NEGATIVE = "validation.order.totalPrice.is_negative";
    public static final String KEY_ADDRESS_IS_NULL = "validation.order.address.is_null";
    public static final String KEY_SHIPMENT_ALREADY_EXISTS = "validation.order.shipment.already_exists";


    public static final ValidationKeys<PriceValueObjectValidationException.Reason> AMOUNT_KEYS =
            new ValidationKeys<>(
                    Map.of(
                            PriceValueObjectValidationException.Reason.NULL,
                            "validation.order.totalAmount.is_null",

                            PriceValueObjectValidationException.Reason.NEGATIVE_NOT_ALLOWED,
                            "validation.order.totalAmount.negative_not_allowed",

                            PriceValueObjectValidationException.Reason.INVALID_SCALE,
                            "validation.order.totalAmount.invalid_scale",

                            PriceValueObjectValidationException.Reason.TOO_SMALL,
                            "validation.order.totalAmount.too_small",

                            PriceValueObjectValidationException.Reason.TOO_LARGE,
                            "validation.order.totalAmount.too_large"
                    )
            );


    public static final ValidationKeys<PriceValueObjectValidationException.Reason> ITEM_KEYS =
            new ValidationKeys<>(
                    Map.of(
                            PriceValueObjectValidationException.Reason.NULL,
                            "validation.order.itemSubtotal.is_null",

                            PriceValueObjectValidationException.Reason.NEGATIVE_NOT_ALLOWED,
                            "validation.order.itemSubtotal.negative_not_allowed",

                            PriceValueObjectValidationException.Reason.INVALID_SCALE,
                            "validation.order.itemSubtotal.invalid_scale",

                            PriceValueObjectValidationException.Reason.TOO_SMALL,
                            "validation.order.itemSubtotal.too_small",

                            PriceValueObjectValidationException.Reason.TOO_LARGE,
                            "validation.order.itemSubtotal.too_large"
                    )
            );


    public static final ValidationKeys<PriceValueObjectValidationException.Reason> SHIPPING_KEYS =
            new ValidationKeys<>(
                    Map.of(
                            PriceValueObjectValidationException.Reason.NULL,
                            "validation.order.shippingCost.is_null",

                            PriceValueObjectValidationException.Reason.NEGATIVE_NOT_ALLOWED,
                            "validation.order.shippingCost.negative_not_allowed",

                            PriceValueObjectValidationException.Reason.INVALID_SCALE,
                            "validation.order.shippingCost.invalid_scale",

                            PriceValueObjectValidationException.Reason.TOO_SMALL,
                            "validation.order.shippingCost.too_small",

                            PriceValueObjectValidationException.Reason.TOO_LARGE,
                            "validation.order.shippingCost.too_large"
                    )
            );
}
