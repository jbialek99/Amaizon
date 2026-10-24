package org.example.amaizon.model.validators.price;

import java.math.BigDecimal;

public record PriceSpec(BigDecimal min, BigDecimal max, int scale) {

    public static final PriceSpec DEFAULT =
            new PriceSpec(new BigDecimal("0.00"), new BigDecimal("1000000000.00"), 2);
}
