package org.example.amaizon.model.validators.price;


import org.example.amaizon.exceptions.price.PriceValueObjectValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PriceValidator {

    private PriceValidator() {}

    private static final PriceSpec spec = PriceSpec.DEFAULT;

    public static BigDecimal validate(BigDecimal raw) {
        if (raw == null) {
            ex(PriceValueObjectValidationException.Reason.NULL);
        }


        if (raw.signum() < 0) {
            ex(PriceValueObjectValidationException.Reason.NEGATIVE_NOT_ALLOWED);
        }

        try {
            raw = raw.setScale(spec.scale(), RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            ex(PriceValueObjectValidationException.Reason.INVALID_SCALE, spec.scale());
        }

        if (raw.compareTo(spec.min()) < 0) {
            ex(PriceValueObjectValidationException.Reason.TOO_SMALL, spec.min().toPlainString());
        }

        if (raw.compareTo(spec.max()) > 0) {
            ex(PriceValueObjectValidationException.Reason.TOO_LARGE, spec.max().toPlainString());
        }

        return raw;
    }

    private static void ex(PriceValueObjectValidationException.Reason reason, Object... args) {
        throw new PriceValueObjectValidationException(reason, args);
    }
}
