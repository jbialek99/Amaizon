package org.example.amaizon.security;

import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.entity.Country;
import org.example.amaizon.model.entity.Role;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.model.entity.UserRole;
import org.example.amaizon.model.enums.RoleKey;
import org.example.amaizon.model.enums.RoleScope;
import org.example.amaizon.model.enums.UserRoleStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StaffAuthorizationGuardTest {

    private final AuthorizationService authorization = new AuthorizationService();
    private final StaffAuthorizationGuard guard = new StaffAuthorizationGuard(authorization);

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void headAdminCanManageGlobalAndBothCountries() {
        authenticate(userWithRole(RoleKey.HEAD_ADMIN, RoleScope.GLOBAL,
                null, UserRoleStatus.ACTIVE, null));

        assertDoesNotThrow(guard::requireGlobalProductAdmin);
        assertDoesNotThrow(() -> guard.requireMarketProductAdmin(market(1L)));
        assertDoesNotThrow(() -> guard.requireMarketProductAdmin(market(2L)));
    }

    @Test
    void polishCountryAdminCannotAccessAmericanMarket() {
        authenticate(userWithRole(RoleKey.COUNTRY_ADMIN, RoleScope.COUNTRY,
                1L, UserRoleStatus.ACTIVE, null));

        assertDoesNotThrow(() -> guard.requireMarketProductAdmin(market(1L)));

        assertThrows(AccessDeniedException.class,
                () -> guard.requireMarketProductAdmin(market(2L)));

        assertThrows(AccessDeniedException.class,
                guard::requireGlobalProductAdmin);
    }

    @Test
    void americanCountryAdminCannotAccessPolishMarket() {
        authenticate(userWithRole(RoleKey.COUNTRY_ADMIN, RoleScope.COUNTRY,
                2L, UserRoleStatus.ACTIVE, null));

        assertDoesNotThrow(() -> guard.requireMarketProductAdmin(market(2L)));

        assertThrows(AccessDeniedException.class,
                () -> guard.requireMarketProductAdmin(market(1L)));
    }

    @Test
    void sellerCannotAdministerProducts() {
        authenticate(userWithRole(RoleKey.SELLER, RoleScope.COUNTRY,
                1L, UserRoleStatus.ACTIVE, null));

        assertThrows(AccessDeniedException.class,
                () -> guard.requireMarketProductAdmin(market(1L)));
    }

    @Test
    void warehouseCannotAdministerProducts() {
        authenticate(userWithRole(RoleKey.WAREHOUSE, RoleScope.COUNTRY,
                1L, UserRoleStatus.ACTIVE, null));

        assertThrows(AccessDeniedException.class,
                () -> guard.requireMarketProductAdmin(market(1L)));
    }

    @Test
    void suspendedRoleIsRejected() {
        authenticate(userWithRole(RoleKey.HEAD_ADMIN, RoleScope.GLOBAL,
                null, UserRoleStatus.SUSPENDED, null));

        assertThrows(AccessDeniedException.class,
                guard::requireGlobalProductAdmin);
    }

    @Test
    void revokedRoleIsRejected() {
        authenticate(userWithRole(RoleKey.COUNTRY_ADMIN, RoleScope.COUNTRY,
                1L, UserRoleStatus.REVOKED, null));

        assertThrows(AccessDeniedException.class,
                () -> guard.requireMarketProductAdmin(market(1L)));
    }

    @Test
    void expiredRoleIsRejected() {
        authenticate(userWithRole(RoleKey.COUNTRY_ADMIN, RoleScope.COUNTRY,
                1L, UserRoleStatus.ACTIVE, Instant.now().minusSeconds(60)));

        assertThrows(AccessDeniedException.class,
                () -> guard.requireMarketProductAdmin(market(1L)));
    }

    @Test
    void anonymousUserIsRejected() {
        assertThrows(AccessDeniedException.class,
                guard::requireGlobalProductAdmin);

        assertThrows(AccessDeniedException.class,
                () -> guard.requireMarketProductAdmin(market(1L)));
    }

    @Test
    void ordinaryUserRoleCannotAccessStaffMarket() {
        User user = userWithRole(RoleKey.USER, RoleScope.COUNTRY,
                1L, UserRoleStatus.ACTIVE, null);

        assertFalse(authorization.canActInCountry(user, 1L));
    }

    private void authenticate(User user) {
        CustomUserDetails details = new CustomUserDetails(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        details,
                        null,
                        details.getAuthorities()
                )
        );
    }

    private User userWithRole(
            RoleKey key,
            RoleScope scope,
            Long countryId,
            UserRoleStatus status,
            Instant validTo
    ) {
        User user = mock(User.class);
        UserRole assignment = mock(UserRole.class);
        Role role = mock(Role.class);

        when(user.getUserRoles()).thenReturn(Set.of(assignment));
        when(assignment.getStatus()).thenReturn(status);
        when(assignment.getValidFrom()).thenReturn(
                Instant.now().minusSeconds(3600)
        );
        when(assignment.getValidTo()).thenReturn(validTo);
        when(assignment.getRole()).thenReturn(role);

        when(role.getKey()).thenReturn(key);
        when(role.getScope()).thenReturn(scope);

        if (countryId != null) {
            Country country = mock(Country.class);
            when(country.getId()).thenReturn(countryId);
            when(role.getCountry()).thenReturn(country);
        }

        return user;
    }

    private MarketContextDto market(long id) {
        MarketContextDto market = mock(MarketContextDto.class);
        Country country = mock(Country.class);

        when(country.getId()).thenReturn(id);
        when(market.getCountry()).thenReturn(country);

        return market;
    }
}
