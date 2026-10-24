package org.example.amaizon.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

public class StaffMarketAccessFilter extends OncePerRequestFilter {

    private final MarketResolver marketResolver;

    public StaffMarketAccessFilter(MarketResolver marketResolver) {
        this.marketResolver = marketResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return !(path.equals("/staff") || path.startsWith("/staff/"));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            filterChain.doFilter(request, response);
            return;
        }

        MarketContextDto market = marketResolver.resolve(request, response);
        request.setAttribute(MarketResolver.REQ_ATTR, market);

        String marketCountryCode = market.getCountry().getCode().getValue();
        User user = cud.getUser();

        boolean allowed = user.getUserRoles().stream()
                .filter(this::isActiveNow)
                .map(UserRole::getRole)
                .anyMatch(role -> isAllowedOnMarket(role, marketCountryCode));

        if (!allowed) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
            response.sendRedirect("/login-staff?error=1");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedOnMarket(Role role, String marketCountryCode) {
        if (role == null || role.getKey() == null || role.getScope() == null) {
            return false;
        }

        if (role.getKey() == RoleKey.HEAD_ADMIN
                && role.getScope() == RoleScope.GLOBAL) {
            return true;
        }

        if (role.getScope() != RoleScope.COUNTRY) {
            return false;
        }

        if (role.getKey() == RoleKey.USER || role.getCountry() == null) {
            return false;
        }

        String roleCountryCode = role.getCountry().getCode().getValue();

        return marketCountryCode.equalsIgnoreCase(roleCountryCode);
    }

    private boolean isActiveNow(UserRole userRole) {
        if (userRole == null || userRole.getStatus() != UserRoleStatus.ACTIVE) {
            return false;
        }

        Instant now = Instant.now();

        if (userRole.getValidFrom() != null && now.isBefore(userRole.getValidFrom())) {
            return false;
        }

        return userRole.getValidTo() == null || !now.isAfter(userRole.getValidTo());
    }
}
