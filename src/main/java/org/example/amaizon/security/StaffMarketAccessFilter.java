package org.example.amaizon.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.repositories.UserRepository;
import org.example.amaizon.resolvers.MarketResolver;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class StaffMarketAccessFilter extends OncePerRequestFilter {

    private final MarketResolver marketResolver;
    private final AuthorizationService authorizationService;
    private final UserRepository userRepository;

    public StaffMarketAccessFilter(
            MarketResolver marketResolver,
            AuthorizationService authorizationService,
            UserRepository userRepository
    ) {
        this.marketResolver = marketResolver;
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath != null && !contextPath.isEmpty()
                && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        return !path.startsWith("/staff/")
                || path.equals("/staff/logout");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        User freshUser = userRepository.findWithRolesByEmail(details.getUsername())
                .orElse(null);

        if (freshUser == null) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        MarketContextDto market = marketResolver.resolve(request, response);

        if (market == null || market.getCountry() == null
                || !authorizationService.canActInCountry(
                        freshUser,
                        market.getCountry().getId())) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        CustomUserDetails refreshed = new CustomUserDetails(freshUser);

        UsernamePasswordAuthenticationToken updated =
                new UsernamePasswordAuthenticationToken(
                        refreshed,
                        null,
                        refreshed.getAuthorities()
                );

        updated.setDetails(authentication.getDetails());

        SecurityContextHolder.getContext().setAuthentication(updated);

        request.setAttribute(MarketResolver.REQ_ATTR, market);
        chain.doFilter(request, response);
    }
}
