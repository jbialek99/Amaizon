package org.example.amaizon.model.validators.validation;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;

public class UserRoleValidation {
    public static final String KEY_USER_IS_NULL = "validation.user_roles.user.is_null";
    public static final String KEY_ROLE_IS_NULL = "validation.user_roles.role.is_null";
    public static final String KEY_STATUS_IS_NULL = "validation.user_roles.status.is_null";
    public static final String KEY_VALID_FROM_IS_NULL = "validation.user_roles.validFrom.is_null";
    public static final String KEY_VALID_TO_BEFORE_VALID_FROM = "validation.user_roles.validTo.before_validFrom";

    public static final TextSpec DESCRIPTION_SPEC = TextSpec.max(5000);

    public static ValidationKeys<TextValueObjectValidationException.Reason> DESCRIPTION_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.user_roles.description.is_null",

                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.user_roles.description.is_empty",

                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.user_roles.description.too_short",

                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.user_roles.description.too_long",

                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.user_roles.description.invalid_length",

                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.user_roles.description.invalid"

            ));

}
