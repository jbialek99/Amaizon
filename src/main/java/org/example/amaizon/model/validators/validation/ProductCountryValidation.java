package org.example.amaizon.model.validators.validation;

public class ProductCountryValidation {

    public static final String KEY_PRODUCT_IS_NULL = "validation.product_countries.product.is_null";
    public static final String KEY_COUNTRY_IS_NULL = "validation.product_countries.country.is_null";

    public static final String KEY_ACTIVATE_WITH_INACTIVE_COUNTRY =
            "validation.product_countries.activate_with_inactive_country";
    public static final String KEY_ACTIVATE_WITHOUT_TRANSLATION =
            "validation.product_countries.activate_without_translation";
    public static final String KEY_ACTIVATE_WITHOUT_PRICE =
            "validation.product_countries.activate_without_price";
    public static final String KEY_ACTIVATE_WITHOUT_CURRENT_PRICE =
            "validation.product_countries.activate_without_current_price";

    public static final String KEY_TRANSLATION_ALREADY_DEFINED =
            "validation.product_countries.translation.already_defined";
    public static final String KEY_TRANSLATION_NOT_DEFINED =
            "validation.product_countries.translation.not_defined";


    public static final String KEY_PRICE_NOT_DEFINED =
            "validation.product_countries.price.not_defined";
}
