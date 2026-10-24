package org.example.amaizon.model;

import org.example.amaizon.exceptions.validation.DomainValidationException;
import org.example.amaizon.model.entity.Country;
import org.example.amaizon.model.entity.Currency;
import org.example.amaizon.model.entity.Domain;
import org.example.amaizon.model.enums.CountryStatus;
import org.example.amaizon.model.enums.DomainStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainTest {

    @Test
    void activate_setsDomainActive_andActivatesCountry() {
        Currency cur = Currency.create("Zloty", "PLN", "zł");
        Country pl = Country.create("Poland", "PL", "pl-PL", cur);
        assertEquals(CountryStatus.DRAFT, pl.getStatus());

        Domain d = Domain.create("amaizon.pl", "pl", pl);
        assertEquals(DomainStatus.DRAFT, d.getStatus());

        d.activate();

        assertEquals(DomainStatus.ACTIVE, d.getStatus());
        assertEquals(CountryStatus.ACTIVE, pl.getStatus());
        assertSame(d, pl.getDomain());
    }

    @Test
    void deactivate_setsDomainInactive_andDeactivatesCountry() {
        Currency cur = Currency.create("Zloty", "PLN", "zł");
        Country pl = Country.create("Poland", "PL", "pl-PL", cur);
        Domain d = Domain.create("amaizon.pl", "pl", pl);
        d.activate();

        d.deactivate();

        assertEquals(DomainStatus.INACTIVE, d.getStatus());
        assertEquals(CountryStatus.INACTIVE, pl.getStatus());
    }

    @Test
    void changeCountry_whenNewCountryAlreadyHasDomain_throws() {
        Currency cur = Currency.create("Zloty", "PLN", "zł");
        Country pl = Country.create("Poland", "PL", "pl-PL", cur);
        Country us = Country.create("United States", "US", "en-US", cur);

        Domain d1 = Domain.create("amaizon.pl", "pl", pl);
        Domain d2 = Domain.create("amaizon.us", "us", us);

        assertThrows(DomainValidationException.class, () -> d1.changeCountry(us));
        assertSame(d2, us.getDomain());
        assertSame(d1, pl.getDomain());
    }
}
