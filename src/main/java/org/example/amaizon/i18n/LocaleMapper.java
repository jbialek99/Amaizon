package org.example.amaizon.i18n;

import org.example.amaizon.exceptions.UnknownMarketException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
public class LocaleMapper {

    private static final Map<String, Locale> TLD_TO_LOCALE = Map.of(
            "pl", Locale.forLanguageTag("pl-PL"),
            "com", Locale.forLanguageTag("en-US")
    );

    public Locale resolveLocale(String tld) {
        if (tld == null || tld.isBlank()) {
            throw internalError(
                    "error.locale_mapper.tld.is_missing"
            );
        }

        String normalized = tld.trim().toLowerCase(Locale.ROOT);

        Locale locale = TLD_TO_LOCALE.get(normalized);

        if (locale == null) {
            throw internalError(
                    "error.locale_mapper.locale.not_configured",
                    normalized
            );
        }

        return locale;
    }

    private static UnknownMarketException internalError(
            String key,
            Object... args
    ) {
        return new UnknownMarketException(
                key,
                HttpStatus.INTERNAL_SERVER_ERROR,
                args
        );
    }
}
