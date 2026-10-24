package org.example.amaizon.model.validators.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.amaizon.model.dtos.CheckoutRequest;
import org.example.amaizon.model.enums.CustomerType;
import org.example.amaizon.model.enums.UsState;

public class ValidCheckoutAddressValidator implements ConstraintValidator<ValidCheckoutAddress, CheckoutRequest> {

    private static final String KEY_STATE_EMPTY = "validation.address_snapshot.state.empty";
    private static final String KEY_STATE_NOT_ALLOWED = "validation.address_snapshot.state.not_allowed";
    private static final String KEY_STATE_INVALID = "validation.address_snapshot.state.invalid";

    private static final String KEY_POSTAL_EMPTY = "validation.address_snapshot.postal.empty";
    private static final String KEY_POSTAL_NOT_ALLOWED = "validation.address_snapshot.postal.not_allowed";

    private static final String KEY_COMPANY_NAME_EMPTY = "validation.company.name.empty";
    private static final String KEY_TAX_ID_EMPTY = "validation.company.taxId.empty";
    private static final String KEY_TAX_ID_INVALID = "validation.company.taxId.invalid";

    @Override
    public boolean isValid(CheckoutRequest v, ConstraintValidatorContext ctx) {
        if (v == null) return true;

        boolean ok = true;
        ctx.disableDefaultConstraintViolation();


        ok &= validateStateByCountry(ctx, "shippingState", v.getShippingCountryCode(), v.getShippingState());
        ok &= validatePostalByCountry(ctx, "shippingPostalCode", v.getShippingCountryCode(), v.getShippingPostalCode());


        ok &= validateStateByCountry(ctx, "billingState", v.getBillingCountryCode(), v.getBillingState());
        ok &= validatePostalByCountry(ctx, "billingPostalCode", v.getBillingCountryCode(), v.getBillingPostalCode());


        if (v.getBillingCustomerType() == CustomerType.COMPANY) {

            if (isBlank(v.getBillingCompanyName())) {
                ok = false;
                violation(ctx, "billingCompanyName", KEY_COMPANY_NAME_EMPTY);
            }

            if (isBlank(v.getBillingTaxId())) {
                ok = false;
                violation(ctx, "billingTaxId", KEY_TAX_ID_EMPTY);
            } else {
                if (isPL(v.getBillingCountryCode()) && !isValidNip(v.getBillingTaxId())) {
                    ok = false;
                    violation(ctx, "billingTaxId", KEY_TAX_ID_INVALID);
                }
            }
        }

        return ok;
    }


    private boolean validateStateByCountry(
            ConstraintValidatorContext ctx,
            String field,
            String countryCode,
            String stateValue
    ) {
        boolean ok = true;
        boolean us = isUS(countryCode);

        if (us) {
            if (isBlank(stateValue)) {
                ok = false;
                violation(ctx, field, KEY_STATE_EMPTY);
                return ok;
            }

            if (!isValidUsState(stateValue)) {
                ok = false;
                violation(ctx, field, KEY_STATE_INVALID);
            }

            return ok;
        }


        if (!isBlank(stateValue)) {
            ok = false;
            violation(ctx, field, KEY_STATE_NOT_ALLOWED);
        }

        return ok;
    }

    private static boolean isValidUsState(String raw) {
        if (raw == null) return false;
        String s = raw.trim();
        if (s.isEmpty()) return false;

        try {
            UsState.valueOf(s.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private boolean validatePostalByCountry(
            ConstraintValidatorContext ctx,
            String field,
            String countryCode,
            String postalValue
    ) {
        boolean ok = true;
        boolean hasPostal = hasPostalCode(countryCode);

        if (hasPostal && isBlank(postalValue)) {
            ok = false;
            violation(ctx, field, KEY_POSTAL_EMPTY);
        }

        if (!hasPostal && !isBlank(postalValue)) {
            ok = false;
            violation(ctx, field, KEY_POSTAL_NOT_ALLOWED);
        }

        return ok;
    }

    private static boolean hasPostalCode(String countryCode) {
        if (countryCode == null) return true;
        String cc = countryCode.trim().toUpperCase();
        return true;
    }

    private static void violation(ConstraintValidatorContext ctx, String field, String key) {
        ctx.buildConstraintViolationWithTemplate("{" + key + "}")
                .addPropertyNode(field)
                .addConstraintViolation();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static boolean isUS(String countryCode) {
        if (countryCode == null) return false;
        String cc = countryCode.trim().toUpperCase();
        return "US".equals(cc) || "USA".equals(cc);
    }

    private static boolean isPL(String countryCode) {
        if (countryCode == null) return false;
        return "PL".equals(countryCode.trim().toUpperCase());
    }

    private static boolean isValidNip(String raw) {
        String nip = raw.replaceAll("[^0-9]", "");
        if (nip.length() != 10) return false;

        int[] w = {6, 5, 7, 2, 3, 4, 5, 6, 7};
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int d = nip.charAt(i) - '0';
            sum += d * w[i];
        }
        int mod = sum % 11;
        if (mod == 10) return false;

        int last = nip.charAt(9) - '0';
        return mod == last;
    }
}
