package org.example.amaizon.security;

import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class StaffAuthorizationGuard {

    private final AuthorizationService authorizationService;

    public StaffAuthorizationGuard(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    public void requireGlobalProductAdmin() {
        if (!authorizationService.canManageGlobalProducts(currentUser())) {
            throw new AccessDeniedException("Global product administration denied");
        }
    }

    public void requireMarketProductAdmin(MarketContextDto market) {
        if (market == null || market.getCountry() == null) {
            throw new AccessDeniedException("Market context required");
        }

        if (!authorizationService.canManageAdminPanel(
                currentUser(), market.getCountry().getId())) {
            throw new AccessDeniedException("Market product administration denied");
        }
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof CustomUserDetails details)) {
            throw new AccessDeniedException("Staff authentication required");
        }

        return details.getUser();
    }
}
