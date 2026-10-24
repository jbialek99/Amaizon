package org.example.amaizon.model.embeddable.all;


import jakarta.persistence.Embeddable;
import org.example.amaizon.exceptions.phoneNumber.PhoneNumberValidationException;

import java.util.Objects;
import java.util.regex.Pattern;



@Embeddable
public class PhoneNumber {
    private final static int PHONE_NUMBER_MIN = 7, PHONE_NUMBER_MAX = 15;
    private final static Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{7,15}$");

    private final static String KEY_NULL ="validation.phoneNumber.is_null";
    private final static String KEY_EMPTY ="validation.phoneNumber.is_empty";
    private final static String KEY_TOO_SHORT ="validation.phoneNumber.too_short";
    private final static String KEY_TOO_LONG ="validation.phoneNumber.too_long";
    private final static String KEY_INVALID="validation.phoneNumber.invalid";

    private String value;

    protected PhoneNumber() {
    }

    public PhoneNumber(String phoneNumber) {
        if (phoneNumber == null)
                ex(KEY_NULL);
        String trimmed = phoneNumber.trim();
        if(trimmed.isEmpty())
            ex(KEY_EMPTY);
        if(trimmed.length() < PHONE_NUMBER_MIN)
            ex(KEY_TOO_SHORT, PHONE_NUMBER_MIN);
        if(trimmed.length() > PHONE_NUMBER_MAX)
            ex(KEY_TOO_LONG, PHONE_NUMBER_MAX);
        if(!PHONE_PATTERN.matcher(trimmed).matches())
            ex(KEY_INVALID);
        this.value = trimmed;
    }

    public String getValue() {
        return value;
    }



    private static void ex(String key, Object... args) {
        throw new PhoneNumberValidationException(key, args);
    }


    @Override
    public String toString() {
        return value;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
