package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.price.PriceValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;

import java.util.Map;

public class ProductPriceValidation {

    public static final String KEY_STATUS_NULL="validation.product_prices.status.is_null";
    public static final String KEY_VALID_TO_IN_PAST="validation.product_prices.valid_to.in_past";
    public static final String KEY_PRODUCT_COUNTRY_NULL = "validation.product_prices.productCountry.is_null";

    public static final ValidationKeys<PriceValueObjectValidationException.Reason> PRICE_KEYS =
            new ValidationKeys<>(
                    Map.of(
                            PriceValueObjectValidationException.Reason.NULL,
                            "validation.product_prices.price.is_null",

                            PriceValueObjectValidationException.Reason.NEGATIVE_NOT_ALLOWED,
                            "validation.product_prices.price.negative_not_allowed",

                            PriceValueObjectValidationException.Reason.INVALID_SCALE,
                            "validation.product_prices.price.invalid_scale",

                            PriceValueObjectValidationException.Reason.TOO_SMALL,
                            "validation.product_prices.price.too_small",

                            PriceValueObjectValidationException.Reason.TOO_LARGE,
                            "validation.product_prices.price.too_large"
                    )
            );


}
