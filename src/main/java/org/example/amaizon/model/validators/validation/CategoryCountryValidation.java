package org.example.amaizon.model.validators.validation;

public class CategoryCountryValidation {

    public static final String KEY_CATEGORY_IS_NULL = "validation.category_country.category.is_null";
    public static final String KEY_COUNTRY_IS_NULL = "validation.category_country.country.is_null";

    public static final String KEY_TRANSLATION_ALREADY_EXISTS =
            "validation.category_countries.translation_already_exists";

    public static final String KEY_TRANSLATION_NOT_DEFINED =
            "validation.category_countries.translation_not_defined";

    public static final String KEY_ACTIVATE_WITH_INACTIVE_COUNTRY =
            "validation.category_countries.activate_with_inactive_country";
    public static final String KEY_ACTIVATE_WITH_INACTIVE_CATEGORY =
            "validation.category_countries.activate_with_inactive_category";
    public static final String KEY_ACTIVATE_WITHOUT_TRANSLATION =
            "validation.category_countries.activate_without_translation";
}
