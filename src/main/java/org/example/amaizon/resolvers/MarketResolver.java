package org.example.amaizon.resolvers;

import com.google.common.net.InternetDomainName;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.amaizon.exceptions.UnknownMarketException;
import org.example.amaizon.i18n.LocaleMapper;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.entity.Country;
import org.example.amaizon.model.entity.Currency;
import org.example.amaizon.model.enums.DomainStatus;
import org.example.amaizon.repositories.CountryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Component
public class MarketResolver {

    public static final String REQ_ATTR = "MARKET_CONTEXT";

    private final LocaleMapper localeMapper;
    private final LocaleResolver localeResolver;
    private final CountryRepository countryRepository;

    public MarketResolver(LocaleMapper localeMapper,
                          LocaleResolver localeResolver,
                          CountryRepository countryRepository) {
        this.localeMapper = localeMapper;
        this.localeResolver = localeResolver;
        this.countryRepository = countryRepository;
    }

    public MarketContextDto resolve(HttpServletRequest req, HttpServletResponse res) {
        String host = normalize(req.getServerName());
        String tld = extractTld(host);

        Country country = countryRepository
                .findByDomainTldAndStatus(tld, DomainStatus.ACTIVE)
                .orElseThrow(() -> unsupportedMarket(tld));

        Locale locale = localeMapper.resolveLocale(tld);
        localeResolver.setLocale(req, res, locale);

        return new MarketContextDto(
                country,
                requireCurrency(country),
                locale,
                requireLanguageCode(country)
        );
    }

    private String normalize(String host) {
        if (host == null || host.isBlank())
            throw invalidHost(host);


        String normalized = host.trim().toLowerCase(Locale.ROOT);

        int portSeparator = normalized.indexOf(':');
        if (portSeparator > 0)
            normalized = normalized.substring(0, portSeparator);


        if (normalized.isBlank())
            throw invalidHost(host);


        if ("localhost".equals(normalized) || normalized.startsWith("127."))
            throw invalidHost(host);


        return normalized;
    }

    private String extractTld(String host) {
        try {
            InternetDomainName domain = InternetDomainName.from(host);

            if(domain.publicSuffix() == null)
                throw invalidHost(host);

            if (!domain.hasPublicSuffix())
                throw invalidHost(host);


            return domain.publicSuffix().toString();

        } catch (IllegalArgumentException e) {
            throw invalidHost(host);
        }
    }

    private static Currency requireCurrency(Country country) {
        if (country.getCurrency() == null) {
            throw invalidMarketConfiguration(
                    "error.market.country.currency.is_missing",
                    country.getId()
            );
        }

        return country.getCurrency();
    }

    private static String requireLanguageCode(Country country) {
        if (country.getLanguageCode() == null) {
            throw invalidMarketConfiguration(
                    "error.market.country.language_code.is_missing",
                    country.getId()
            );
        }

        String value = country.getLanguageCode().getValue();

        if (value == null || value.isBlank()) {
            throw invalidMarketConfiguration(
                    "error.market.country.language_code.value.is_missing",
                    country.getId()
            );
        }

        return value.trim();
    }

    private static UnknownMarketException invalidHost(Object... args) {
        return new UnknownMarketException(
                "error.market.invalid_host",
                HttpStatus.BAD_REQUEST,
                args
        );
    }

    private static UnknownMarketException unsupportedMarket(Object... args) {
        return new UnknownMarketException(
                "error.market.unsupported",
                HttpStatus.NOT_FOUND,
                args
        );
    }

    private static UnknownMarketException invalidMarketConfiguration(String keyMessage, Object... args) {
        return new UnknownMarketException(
                keyMessage,
                HttpStatus.INTERNAL_SERVER_ERROR,
                args
        );
    }
}
