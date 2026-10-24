package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;

public class ShipmentValidation {

    public static final String KEY_ORDER_IS_NULL = "validation.shipment.order.is_null";
    public static final String KEY_ALREADY_FINAL = "validation.shipment.status.delivered_or_cancelled";
    public static final String KEY_CARRIER_NULL = "validation.shipment.carrier.is_null";
    public static final String KEY_DELIVER_ONLY_AFTER_SHIPPED ="validation.shipment.deliver_only_after_shipped";
    public static final String KEY_TRACKING_NUMBER_NULL = "validation.shipment.trackingNumber.is_null";


    public static final TextSpec TRACKING_NUMBER_SPEC = TextSpec.length(6,120);

    public static final ValidationKeys<TextValueObjectValidationException.Reason> TRACKING_NUMBER_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.shipment.trackingNumber.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.shipment.trackingNumber.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.shipment.trackingNumber.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.shipment.trackingNumber.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.shipment.trackingNumber.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.shipment.trackingNumber.invalid"
            )
    );

}
