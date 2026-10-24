package org.example.amaizon.services;

import org.example.amaizon.model.enums.Carrier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@Component
public class ShippingPricing {

    public Map<Carrier, BigDecimal> availableOptions(String countryCode) {
        String code = normalizeCountryCode(countryCode);
        EnumMap<Carrier, BigDecimal> prices = new EnumMap<>(Carrier.class);

        switch (code) {
            case "PL" -> {
                prices.put(Carrier.INPOST, new BigDecimal("12.99"));
                prices.put(Carrier.DPD, new BigDecimal("19.99"));
                prices.put(Carrier.DHL, new BigDecimal("24.99"));
                prices.put(Carrier.GLS, new BigDecimal("18.99"));
            }
            case "US" -> {
                prices.put(Carrier.DPD, new BigDecimal("9.99"));
                prices.put(Carrier.DHL, new BigDecimal("14.99"));
                prices.put(Carrier.GLS, new BigDecimal("11.99"));
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported shipping country: " + code
            );
        }

        return Collections.unmodifiableMap(prices);
    }

    public BigDecimal price(String countryCode, Carrier carrier) {
        if (carrier == null) {
            throw new IllegalArgumentException("Shipping carrier required");
        }

        BigDecimal price = availableOptions(countryCode).get(carrier);

        if (price == null) {
            throw new IllegalArgumentException(
                    "Shipping carrier unavailable for selected market"
            );
        }

        return price;
    }

    private String normalizeCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            throw new IllegalArgumentException("Shipping country required");
        }

        return countryCode.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
