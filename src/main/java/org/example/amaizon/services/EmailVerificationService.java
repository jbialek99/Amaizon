package org.example.amaizon.services;

import jakarta.transaction.Transactional;
import org.example.amaizon.exceptions.validation.UserValidationException;
import org.example.amaizon.model.entity.EmailVerificationToken;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.repositories.EmailVerificationTokenRepository;
import org.example.amaizon.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepo;
    private final UserRepository userRepo;

    public EmailVerificationService(EmailVerificationTokenRepository tokenRepo, UserRepository userRepo) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public void verify(String token) {
        EmailVerificationToken t = tokenRepo.findByToken(token)
                .orElseThrow(() -> new UserValidationException("validation.user.email.token.invalid"));

        if (t.isUsed()) {
            throw new UserValidationException("validation.user.email.token.used");
        }
        if (t.isExpired()) {
            throw new UserValidationException("validation.user.email.token.expired");
        }

        User user = t.getUser();
        user.markEmailVerified();
        t.markUsed();

    }
}
