package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.price.PriceValueObjectValidationException;
import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;

public class PaymentValidation {


    public static final String KEY_ORDER_IS_NULL ="validation.payment.order.is_null";
    public static final String KEY_CURRENCY_IS_NULL = "validation.payment.currency.is_null";
    public static final String KEY_AMOUNT_INVALID = "validation.payment.amount.invalid";
    public static final String KEY_ALREADY_FINAL = "validation.payment.already_final";
    public static final String PAYMENT_REFUND_ONLY_AFTER_SUCCESS = "validation.payment.refund_only_after_success";

    public static final String   PAYMENT_CANNOT_MARK_SUCCESS_AFTER_FAILED = "validation.payment.cannot_mark_success_after_failed";



    public static final ValidationKeys<PriceValueObjectValidationException.Reason> AMOUNT_KEYS =
            new ValidationKeys<>(
                    Map.of(
                            PriceValueObjectValidationException.Reason.NULL,
                            "validation.payment.amount.is_null",

                            PriceValueObjectValidationException.Reason.NEGATIVE_NOT_ALLOWED,
                            "validation.payment.amount.negative_not_allowed",

                            PriceValueObjectValidationException.Reason.INVALID_SCALE,
                            "validation.payment.amount.invalid_scale",

                            PriceValueObjectValidationException.Reason.TOO_SMALL,
                            "validation.payment.amount.too_small",

                            PriceValueObjectValidationException.Reason.TOO_LARGE,
                            "validation.payment.amount.too_large"
                    )
            );



    public static final TextSpec   TRANSACTION_ID_SPEC = TextSpec.length(6,120);

    public static final ValidationKeys<TextValueObjectValidationException.Reason> TRANSACTION_ID_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.payment.transactionId.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.payment.transactionId.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.payment.transactionId.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.payment.transactionId.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.payment.transactionId.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.payment.transactionId.invalid"
            )
    );
}
