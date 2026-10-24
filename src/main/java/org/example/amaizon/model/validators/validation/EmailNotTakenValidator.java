package org.example.amaizon.model.validators.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.amaizon.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class EmailNotTakenValidator implements ConstraintValidator<EmailNotTaken, String> {

    private final UserRepository userRepository;

    public EmailNotTakenValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) return true;

        String email = value.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByEmail_ValueIgnoreCase(email).isEmpty();
    }
}
