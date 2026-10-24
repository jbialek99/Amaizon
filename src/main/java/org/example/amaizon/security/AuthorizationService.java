package org.example.amaizon.security;

import org.example.amaizon.model.entity.Role;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.model.entity.UserRole;
import org.example.amaizon.model.enums.RoleKey;
import org.example.amaizon.model.enums.RoleScope;
import org.example.amaizon.model.enums.UserRoleStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuthorizationService {

    public boolean canManageGlobalProducts(User user) {
        return hasGlobalRole(user, RoleKey.HEAD_ADMIN);
    }

    public boolean canActInCountry(User user, long countryId) {
        if (hasGlobalRole(user, RoleKey.HEAD_ADMIN)) return true;
        return hasAnyRoleInCountry(user, countryId);
    }

    public boolean canManageAdminPanel(User user, long countryId) {
        if (hasGlobalRole(user, RoleKey.HEAD_ADMIN)) return true;
        return hasRoleInCountry(user, RoleKey.COUNTRY_ADMIN, countryId);
    }

    public boolean canSell(User user, long countryId) {
        if (hasGlobalRole(user, RoleKey.HEAD_ADMIN)) return true;
        return hasRoleInCountry(user, RoleKey.SELLER, countryId)
                || hasRoleInCountry(user, RoleKey.COUNTRY_ADMIN, countryId);
    }

    public boolean canWorkWarehouse(User user, long countryId) {
        if (hasGlobalRole(user, RoleKey.HEAD_ADMIN)) return true;
        return hasRoleInCountry(user, RoleKey.WAREHOUSE, countryId)
                || hasRoleInCountry(user, RoleKey.COUNTRY_ADMIN, countryId);
    }

    private boolean hasGlobalRole(User user, RoleKey key) {
        if (user == null) return false;

        Instant now = Instant.now();

        return user.getUserRoles().stream()
                .filter(ur -> isActiveNow(ur, now))
                .map(UserRole::getRole)
                .anyMatch(role -> role != null
                        && role.getScope() == RoleScope.GLOBAL
                        && role.getKey() == key);
    }

    private boolean hasAnyRoleInCountry(User user, long countryId) {
        if (user == null) return false;

        Instant now = Instant.now();

        return user.getUserRoles().stream()
                .filter(ur -> isActiveNow(ur, now))
                .map(UserRole::getRole)
                .anyMatch(role -> isCountryRoleFor(role, countryId)
                        && role.getKey() != RoleKey.USER);
    }

    private boolean hasRoleInCountry(User user, RoleKey key, long countryId) {
        if (user == null) return false;

        Instant now = Instant.now();

        return user.getUserRoles().stream()
                .filter(ur -> isActiveNow(ur, now))
                .map(UserRole::getRole)
                .anyMatch(role -> isCountryRoleFor(role, countryId)
                        && role.getKey() == key);
    }

    private boolean isCountryRoleFor(Role role, long countryId) {
        return role != null
                && role.getScope() == RoleScope.COUNTRY
                && role.getCountry() != null
                && role.getCountry().getId() == countryId;
    }

    private boolean isActiveNow(UserRole assignment, Instant now) {
        return assignment != null
                && assignment.getStatus() == UserRoleStatus.ACTIVE
                && assignment.getValidFrom() != null
                && !now.isBefore(assignment.getValidFrom())
                && (assignment.getValidTo() == null
                    || !now.isAfter(assignment.getValidTo()));
    }
}
