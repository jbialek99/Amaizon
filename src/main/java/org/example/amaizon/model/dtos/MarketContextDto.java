package org.example.amaizon.model.dtos;

import org.example.amaizon.exceptions.UnknownMarketException;
import org.example.amaizon.model.entity.Country;
import org.example.amaizon.model.entity.Currency;
import org.springframework.http.HttpStatus;

import java.util.Locale;

public class MarketContextDto {

    private final Country country;
    private final Currency currency;
    private final Locale locale;
    private final String languageCode;

    public MarketContextDto(Country country,
                            Currency currency,
                            Locale locale,
                            String languageCode) {
        this.country = validateCountry(country);
        this.currency = validateCurrency(currency);
        this.locale = validateLocale(locale);
        this.languageCode = validateLanguageCode(languageCode);
    }

    public Country getCountry() {
        return country;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    private static void invalidContext(String keyMessage, Object... args) {
        throw new UnknownMarketException(
                keyMessage,
                HttpStatus.INTERNAL_SERVER_ERROR,
                args
        );
    }

    private static Country validateCountry(Country country) {
        if (country == null) {
            invalidContext("error.marketContextDto.country.is_null");
        }
        return country;
    }

    private static Currency validateCurrency(Currency currency) {
        if (currency == null) {
            invalidContext("error.marketContextDto.currency.is_null");
        }

        if (currency.getSymbol() == null) {
            invalidContext("error.marketContextDto.currency.symbol.is_null");
        }

        if (currency.getSymbol().getValue() == null
                || currency.getSymbol().getValue().isBlank()) {
            invalidContext("error.marketContextDto.currency.symbol.value.is_null");
        }

        return currency;
    }

    private static Locale validateLocale(Locale locale) {
        if (locale == null) {
            invalidContext("error.marketContextDto.locale.null");
        }
        return locale;
    }

    private static String validateLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.isBlank())
            invalidContext("error.marketContextDto.languageCode.is_null");

        return languageCode.trim();
    }
}
