package org.example.amaizon.web;

import jakarta.servlet.http.HttpServletRequest;

import org.example.amaizon.exceptions.UnknownMarketException;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.resolvers.MarketResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Locale;

@ControllerAdvice
public class MarketModelAdvice {

    @ModelAttribute("market")
    public MarketContextDto market(HttpServletRequest request) {
        Object value = request.getAttribute(MarketResolver.REQ_ATTR);

        if (!(value instanceof MarketContextDto ctx))
            throw missingMarketContext();

        return ctx;
    }

    @ModelAttribute("currencySymbol")
    public String currencySymbol(HttpServletRequest request) {
        return market(request)
                .getCurrency()
                .getSymbol()
                .getValue();
    }

    @ModelAttribute("currentLocale")
    public Locale currentLocale(HttpServletRequest request) {
        return market(request).getLocale();
    }

    private static UnknownMarketException missingMarketContext() {
        return new UnknownMarketException("error.market.context_missing", HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
