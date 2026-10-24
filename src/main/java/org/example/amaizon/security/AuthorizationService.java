package org.example.amaizon.security;

import org.example.amaizon.model.entity.Role;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.model.entity.UserRole;
import org.example.amaizon.model.enums.RoleKey;
import org.example.amaizon.model.enums.RoleScope;

import java.time.Instant;

public class AuthorizationService {

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
        Instant now = Instant.now();
        return user.getUserRoles().stream()
                .filter(ur -> isActiveNow(ur, now))
                .map(UserRole::getRole)
                .anyMatch(r -> r.getScope() == RoleScope.GLOBAL && r.getKey() == key);
    }

    private boolean hasAnyRoleInCountry(User user, long countryId) {
        Instant now = Instant.now();
        return user.getUserRoles().stream()
                .filter(ur -> isActiveNow(ur, now))
                .map(UserRole::getRole)
                .anyMatch(r -> isCountryRoleFor(r, countryId));
    }

    private boolean hasRoleInCountry(User user, RoleKey key, long countryId) {
        Instant now = Instant.now();
        return user.getUserRoles().stream()
                .filter(ur -> isActiveNow(ur, now))
                .map(UserRole::getRole)
                .anyMatch(r -> isCountryRoleFor(r, countryId) && r.getKey() == key);
    }

    private boolean isCountryRoleFor(Role role, long countryId) {
        return role.getScope() == RoleScope.COUNTRY
                && role.getCountry() != null
                && role.getCountry().getId() == countryId;
    }

    private boolean isActiveNow(UserRole ur, Instant now) {
        if (ur.getValidFrom() != null && ur.getValidFrom().isAfter(now)) return false;
        return ur.getValidTo() == null || !ur.getValidTo().isBefore(now);
    }
}
