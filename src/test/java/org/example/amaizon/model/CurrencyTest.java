package org.example.amaizon.model;

import org.example.amaizon.model.entity.Country;
import org.example.amaizon.model.entity.Currency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyTest {

    @Test
    void create_setsFields_andStableId() {
        Currency c = Currency.create("Polish Zloty", "PLN", "zł");

        assertNotNull(c.getStableId());
        assertEquals("polish zloty", c.getName().getValue());
        assertEquals("PLN", c.getCode().getValue());
        assertEquals("zł", c.getSymbol().getValue());
    }

    @Test
    void country_createdWithCurrency_isLinkedInCurrencyCountries() {
        Currency c = Currency.create("Polish Zloty", "PLN", "zł");
        Country pl = Country.create("Poland", "PL", "pl-PL", c);

        assertTrue(c.getCountries().contains(pl));
    }

    @Test
    void changeCurrency_movesCountryBetweenCurrencies() {
        Currency pln = Currency.create("Polish Zloty", "PLN", "zł");
        Currency usd = Currency.create("US Dollar", "USD", "$");

        Country pl = Country.create("Poland", "PL", "pl-PL", pln);
        assertTrue(pln.getCountries().contains(pl));
        assertFalse(usd.getCountries().contains(pl));

        pl.changeCurrency(usd);

        assertFalse(pln.getCountries().contains(pl));
        assertTrue(usd.getCountries().contains(pl));
        assertSame(usd, pl.getCurrency());
    }
}
