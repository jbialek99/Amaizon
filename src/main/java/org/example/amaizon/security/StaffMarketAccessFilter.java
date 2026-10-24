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
    private final AuthorizationService authorizationService;

    public StaffMarketAccessFilter(MarketResolver marketResolver,
                                   AuthorizationService authorizationService) {
        this.marketResolver = marketResolver;
        this.authorizationService = authorizationService;
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

        boolean allowed = authorizationService.canActInCountry(
                user, market.getCountry().getId());

        if (!allowed) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
            response.sendRedirect("/login-staff?error=1");
            return;
        }

        filterChain.doFilter(request, response);
    }

}
