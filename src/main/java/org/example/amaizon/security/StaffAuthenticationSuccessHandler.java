package org.example.amaizon.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.entity.Role;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.model.entity.UserRole;
import org.example.amaizon.model.enums.RoleKey;
import org.example.amaizon.model.enums.RoleScope;
import org.example.amaizon.model.enums.UserRoleStatus;
import org.example.amaizon.resolvers.MarketResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class StaffAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final MarketResolver marketResolver;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public StaffAuthenticationSuccessHandler(MarketResolver marketResolver) {
        this.marketResolver = marketResolver;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {


        MarketContextDto market = marketResolver.resolve(request, response);

        String marketCountryCode = market.getCountry().getCode().getValue();

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails cud)) {
            request.getSession().invalidate();
            redirectStrategy.sendRedirect(request, response, "/staff/login?error=1");
            return;
        }

        User user = cud.getUser();

        boolean allowed = user.getUserRoles().stream()
                .filter(this::isActiveNow)
                .map(UserRole::getRole)
                .anyMatch(role -> isAllowedOnMarket(role, marketCountryCode));

        if (!allowed) {
            request.getSession().invalidate();
            redirectStrategy.sendRedirect(request, response, "/staff/login?error=1");
            return;
        }

        redirectStrategy.sendRedirect(request, response, "/staff/home");

    }

    private boolean isAllowedOnMarket(Role role, String marketCountryCode) {
        if (role == null || role.getKey() == null || role.getScope() == null) return false;


        if (role.getKey() == RoleKey.HEAD_ADMIN && role.getScope() == RoleScope.GLOBAL) {
            return true;
        }

        if (role.getScope() == RoleScope.COUNTRY) {
            if (role.getKey() == RoleKey.USER) return false;
            if (role.getCountry() == null) return false;

            String roleCountryCode = role.getCountry().getCode().getValue();
            return marketCountryCode.equalsIgnoreCase(roleCountryCode);
        }


        return false;
    }

    private boolean isActiveNow(UserRole ur) {
        if (ur == null) return false;
        if (ur.getStatus() != UserRoleStatus.ACTIVE) return false;

        Instant now = Instant.now();
        if (ur.getValidFrom() != null && now.isBefore(ur.getValidFrom())) return false;
        return ur.getValidTo() == null || !now.isAfter(ur.getValidTo());
    }
}
