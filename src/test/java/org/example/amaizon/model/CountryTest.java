package org.example.amaizon.model;

import org.example.amaizon.exceptions.validation.CountryValidationException;
import org.example.amaizon.model.entity.Country;
import org.example.amaizon.model.entity.Currency;
import org.example.amaizon.model.entity.Domain;
import org.example.amaizon.model.enums.CountryStatus;
import org.example.amaizon.model.enums.DomainStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CountryTest {

    @Test
    void activate_withoutDomain_throws() {
        Currency pln = Currency.create("Polish Zloty", "PLN", "zł");
        Country pl = Country.create("Poland", "PL", "pl-PL", pln);

        assertThrows(CountryValidationException.class, pl::activate);
        assertEquals(CountryStatus.DRAFT, pl.getStatus());
    }

    @Test
    void deactivate_whenDomainActive_throws() {
        Currency pln = Currency.create("Polish Zloty", "PLN", "zł");
        Country pl = Country.create("Poland", "PL", "pl-PL", pln);
        Domain d = Domain.create("amaizon.pl", "pl", pl);
        d.activate();

        assertEquals(DomainStatus.ACTIVE, d.getStatus());
        assertEquals(CountryStatus.ACTIVE, pl.getStatus());

        assertThrows(CountryValidationException.class, pl::deactivate);
        assertEquals(CountryStatus.ACTIVE, pl.getStatus());
    }

    @Test
    void deactivate_afterDomainDeactivated_setsCountryInactive() {
        Currency pln = Currency.create("Polish Zloty", "PLN", "zł");
        Country pl = Country.create("Poland", "PL", "pl-PL", pln);
        Domain d = Domain.create("amaizon.pl", "pl", pl);
        d.activate();

        d.deactivate();

        assertEquals(DomainStatus.INACTIVE, d.getStatus());
        assertEquals(CountryStatus.INACTIVE, pl.getStatus());
    }

    @Test
    void activate_whenDomainActive_setsCountryActive() {
        Currency pln = Currency.create("Polish Zloty", "PLN", "zł");
        Country pl = Country.create("Poland", "PL", "pl-PL", pln);
        Domain d = Domain.create("amaizon.pl", "pl", pl);
        d.activate();

        d.deactivate();
        assertEquals(DomainStatus.INACTIVE, d.getStatus());
        assertEquals(CountryStatus.INACTIVE, pl.getStatus());

        d.activate();
        assertEquals(DomainStatus.ACTIVE, d.getStatus());
        assertEquals(CountryStatus.ACTIVE, pl.getStatus());
    }
}
