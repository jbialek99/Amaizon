package org.example.amaizon.services;


import org.example.amaizon.exceptions.EmailAlreadyUsedException;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.dtos.RegisterRequest;
import org.example.amaizon.model.entity.Country;
import org.example.amaizon.model.entity.Role;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.model.enums.RoleKey;
import org.example.amaizon.repositories.RoleRepository;
import org.example.amaizon.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserRepository userRepository,
                                   RoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(MarketContextDto market, RegisterRequest req) {

        String email = normalizeEmail(req.getEmail());

        if (userRepository.findByEmail_ValueIgnoreCase(email).isPresent()) {
            throw new EmailAlreadyUsedException(email);
        }

        String hash = passwordEncoder.encode(req.getPassword());
        User user = User.create(email, hash);

        Role userRole = roleRepository.findByKey(RoleKey.USER)
                .orElseThrow(() -> new IllegalStateException("Role USER not found in database"));

        user.assignRole(
                userRole,
                Instant.now(),
                null,
                "Default user role"
        );
        Country country = market.getCountry();


        String locale = market.getLocale().toLanguageTag();


        user.createCountryProfile(
                country,
                safeTrim(req.getFirstName()),
                safeTrim(req.getLastName()),
                null,
                locale,
                null
        );

        userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
