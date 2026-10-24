package org.example.amaizon.model;

import org.example.amaizon.exceptions.validation.ProductCountryValidationException;
import org.example.amaizon.model.entity.Country;
import org.example.amaizon.model.entity.Currency;
import org.example.amaizon.model.entity.Domain;
import org.example.amaizon.model.entity.ProductCountry;
import org.example.amaizon.model.enums.ProductCountryStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ProductCountryTest {

    @Test
    void activateOrThrow_withoutTranslation_throws() {
        Currency pln = Currency.create("Polish Zloty", "PLN", "zł");
        Country pl = Country.create("Poland", "PL", "pl-PL", pln);
        Domain d = Domain.create("amaizon.pl", "pl", pl);
        d.activate();

        var product = Mockito.mock(org.example.amaizon.model.entity.Product.class);
        Mockito.when(product.getStatus()).thenReturn(org.example.amaizon.model.enums.ProductStatus.ACTIVE);

        ProductCountry pc = ProductCountry.create(product, pl);

        assertThrows(ProductCountryValidationException.class, pc::activateOrThrow);
        assertEquals(ProductCountryStatus.DRAFT, pc.getStatus());
    }

    @Test
    void removeTranslation_whenMissing_isNoop() {
        Currency pln = Currency.create("Polish Zloty", "PLN", "zł");
        Country pl = Country.create("Poland", "PL", "pl-PL", pln);
        Domain d = Domain.create("amaizon.pl", "pl", pl);
        d.activate();

        var product = Mockito.mock(org.example.amaizon.model.entity.Product.class);
        Mockito.when(product.getStatus()).thenReturn(org.example.amaizon.model.enums.ProductStatus.ACTIVE);

        ProductCountry pc = ProductCountry.create(product, pl);

        pc.removeTranslation();

        assertNull(pc.getTranslation());
        assertEquals(ProductCountryStatus.DRAFT, pc.getStatus());
    }
}
