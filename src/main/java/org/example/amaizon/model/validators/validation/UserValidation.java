package org.example.amaizon.model.validators.validation;


import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Map;
import java.util.regex.Pattern;

public class UserValidation {
    public static final String KEY_PASSWORD_IS_EMPTY = "validation.user.password.is_empty";
    public static final String KEY_ROLE_IS_NULL = "validation.user.role.is_null";
    public static final String KEY_ROLE_ALREADY_ASSIGNED = "validation.user.role.already_assigned";
    public static final String KEY_ROLE_NOT_ASSIGNED = "validation.user.role.not_assigned";
    public static final String KEY_ROLE_TO_CHANGE_NOT_FOUND = "validation.user.role.to_change.not_found";

    public static final String KEY_COUNTRY_IS_NULL = "validation.user.country.is_null";
    public static final String KEY_MEMBERSHIP_ALREADY_EXISTS = "validation.user.country.membership.already_exists";
    public static final String KEY_MEMBERSHIP_NOT_EXISTS = "validation.user.country.membership.not_exists";

    public static final String KEY_PROFILE_ALREADY_EXISTS = "validation.user.country.profile.already_exists";
    public static final String KEY_PROFILE_NOT_EXISTS = "validation.user.country.profile.not_exists";

    public static final String KEY_WORKPLACE_IS_NULL = "validation.user.workplace.is_null";
    public static final String KEY_USER_WORKPLACE_ALREADY_EXISTS = "validation.user.workplace.already_exists";
    public static final String KEY_USER_WORKPLACE_NOT_EXISTS = "validation.user.workplace.not_exists";

    public static final TextSpec EMAIL_SPEC = TextSpec.pattern(6,100, Pattern.compile("^(?!\\.)[A-Za-z0-9._%+-]+(?<!\\.)@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"));

    public static final ValidationKeys<TextValueObjectValidationException.Reason> EMAIL_KEYS = new ValidationKeys<>(
            Map.of(
                    TextValueObjectValidationException.Reason.NULL,
                    "validation.user.email.is_null",
                    TextValueObjectValidationException.Reason.EMPTY,
                    "validation.user.email.is_empty",
                    TextValueObjectValidationException.Reason.TOO_SHORT,
                    "validation.user.email.too_short",
                    TextValueObjectValidationException.Reason.TOO_LONG,
                    "validation.user.email.too_long",
                    TextValueObjectValidationException.Reason.INVALID_LENGTH,
                    "validation.user.email.invalid_length",
                    TextValueObjectValidationException.Reason.INVALID_FORMAT,
                    "validation.user.email.invalid"


            )
    );


}
