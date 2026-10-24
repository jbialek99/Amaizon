package org.example.amaizon.model.validators.validation;

public final class ProductStockValidation {

    private ProductStockValidation() {}

    public static final String KEY_PRODUCT_COUNTRY_IS_NULL =
            "validation.product_stocks.productCountry.is_null";

    public static final String KEY_QTY_NEGATIVE =
            "validation.product_stocks.quantity.negative";

    public static final String KEY_NOT_ENOUGH_AVAILABLE =
            "validation.product_stocks.quantity_available.not_enough";

    public static final String KEY_NOT_ENOUGH_RESERVED =
            "validation.product_stocks.quantity_reserved.not_enough";
}
