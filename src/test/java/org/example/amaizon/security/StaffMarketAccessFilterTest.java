package org.example.amaizon.security;

import jakarta.servlet.FilterChain;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.entity.Country;
import org.example.amaizon.model.entity.Role;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.model.entity.UserRole;
import org.example.amaizon.model.enums.RoleKey;
import org.example.amaizon.model.enums.RoleScope;
import org.example.amaizon.model.enums.UserRoleStatus;
import org.example.amaizon.repositories.UserRepository;
import org.example.amaizon.resolvers.MarketResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StaffMarketAccessFilterTest {

    private final MarketResolver resolver = mock(MarketResolver.class);
    private final UserRepository users = mock(UserRepository.class);
    private final AuthorizationService authorization = new AuthorizationService();
    private final StaffMarketAccessFilter filter =
            new StaffMarketAccessFilter(resolver, authorization, users);

    private final FilterChain chain = mock(FilterChain.class);
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeEach
    void setup() {
        request.setRequestURI("/staff/products");
        request.setMethod("GET");

        MarketContextDto market = mock(MarketContextDto.class);
        Country country = mock(Country.class);

        when(country.getId()).thenReturn(1L);
        when(market.getCountry()).thenReturn(country);
        when(resolver.resolve(any(), any())).thenReturn(market);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void activeRoleAllowsRequest() throws Exception {
        authenticate(roleUser(RoleKey.COUNTRY_ADMIN, UserRoleStatus.ACTIVE, 1L));
        User freshUser = roleUser(RoleKey.COUNTRY_ADMIN, UserRoleStatus.ACTIVE, 1L);

        when(users.findWithRolesByEmail("staff@example.com"))
                .thenReturn(Optional.of(freshUser));

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        verify(chain).doFilter(request, response);
    }

    @Test
    void revokedRoleInDatabaseBlocksOldSession() throws Exception {
        authenticate(roleUser(RoleKey.COUNTRY_ADMIN, UserRoleStatus.ACTIVE, 1L));
        User freshUser = roleUser(RoleKey.COUNTRY_ADMIN, UserRoleStatus.REVOKED, 1L);

        when(users.findWithRolesByEmail("staff@example.com"))
                .thenReturn(Optional.of(freshUser));

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        verifyNoInteractions(chain);
    }

    @Test
    void employeeFromDifferentCountryIsForbidden() throws Exception {
        authenticate(roleUser(RoleKey.COUNTRY_ADMIN, UserRoleStatus.ACTIVE, 1L));
        User freshUser = roleUser(RoleKey.COUNTRY_ADMIN, UserRoleStatus.ACTIVE, 2L);

        when(users.findWithRolesByEmail("staff@example.com"))
                .thenReturn(Optional.of(freshUser));

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        verifyNoInteractions(chain);
    }

    @Test
    void deletedAccountIsForbidden() throws Exception {
        authenticate(roleUser(RoleKey.HEAD_ADMIN, UserRoleStatus.ACTIVE, null));
        when(users.findWithRolesByEmail("staff@example.com"))
                .thenReturn(Optional.empty());

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        verifyNoInteractions(chain);
    }

    private void authenticate(User user) {
        CustomUserDetails details = new CustomUserDetails(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        details,
                        null,
                        List.copyOf(details.getAuthorities())
                )
        );
    }

    private User roleUser(RoleKey key, UserRoleStatus status, Long countryId) {
        User user = mock(User.class);
        UserRole assignment = mock(UserRole.class);
        Role role = mock(Role.class);

        when(user.getEmail()).thenReturn("staff@example.com");
        when(user.getUserRoles()).thenReturn(Set.of(assignment));

        when(assignment.getStatus()).thenReturn(status);
        when(assignment.getValidFrom()).thenReturn(
                Instant.now().minusSeconds(3600)
        );
        when(assignment.getRole()).thenReturn(role);

        when(role.getKey()).thenReturn(key);
        when(role.getScope()).thenReturn(
                countryId == null ? RoleScope.GLOBAL : RoleScope.COUNTRY
        );

        if (countryId != null) {
            Country country = mock(Country.class);
            when(country.getId()).thenReturn(countryId);
            when(role.getCountry()).thenReturn(country);
        }

        return user;
    }
}
