package org.example.amaizon.services;

import org.example.amaizon.model.enums.Carrier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ShippingPricingTest {

    private final ShippingPricing pricing = new ShippingPricing();

    @Test
    void polishMarketUsesPlnDemoRates() {
        Map<Carrier, BigDecimal> options = pricing.availableOptions("PL");

        assertEquals(4, options.size());
        assertEquals(new BigDecimal("12.99"), options.get(Carrier.INPOST));
        assertEquals(new BigDecimal("19.99"), options.get(Carrier.DPD));
        assertEquals(new BigDecimal("24.99"), options.get(Carrier.DHL));
        assertEquals(new BigDecimal("18.99"), options.get(Carrier.GLS));
    }

    @Test
    void americanMarketUsesUsdDemoRates() {
        Map<Carrier, BigDecimal> options = pricing.availableOptions("US");

        assertEquals(3, options.size());
        assertEquals(new BigDecimal("9.99"), options.get(Carrier.DPD));
        assertEquals(new BigDecimal("14.99"), options.get(Carrier.DHL));
        assertEquals(new BigDecimal("11.99"), options.get(Carrier.GLS));
        assertFalse(options.containsKey(Carrier.INPOST));
    }

    @Test
    void rejectsUnavailableCarrier() {
        assertThrows(
                IllegalArgumentException.class,
                () -> pricing.price("US", Carrier.INPOST)
        );
    }

    @Test
    void rejectsUnsupportedMarket() {
        assertThrows(
                IllegalArgumentException.class,
                () -> pricing.availableOptions("DE")
        );
    }

    @Test
    void normalizesCountryCode() {
        assertEquals(
                new BigDecimal("14.99"),
                pricing.price(" us ", Carrier.DHL)
        );
    }

    @Test
    void rejectsMissingCarrier() {
        assertThrows(
                IllegalArgumentException.class,
                () -> pricing.price("PL", null)
        );
    }

    @Test
    void priceMatchesDisplayedOptions() {
        for (String country : new String[]{"PL", "US"}) {
            pricing.availableOptions(country).forEach(
                    (carrier, amount) ->
                            assertEquals(amount, pricing.price(country, carrier))
            );
        }
    }

    @Test
    void cannotModifyAvailableOptions() {
        Map<Carrier, BigDecimal> options = pricing.availableOptions("US");

        assertThrows(
                UnsupportedOperationException.class,
                () -> options.put(Carrier.INPOST, BigDecimal.ZERO)
        );
    }
}
