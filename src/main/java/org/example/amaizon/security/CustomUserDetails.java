package org.example.amaizon.security;


import org.example.amaizon.model.entity.User;
import org.example.amaizon.model.entity.UserRole;
import org.example.amaizon.model.enums.UserRoleStatus;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;



import java.time.Instant;
import java.util.Collection;



public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Instant now = Instant.now();
        return user.getUserRoles().stream()
                .filter(ur -> ur.getStatus() == UserRoleStatus.ACTIVE)
                .filter(ur -> ur.getValidFrom() != null && now.isAfter(ur.getValidFrom()))
                .filter(ur -> ur.getValidTo() == null || now.isBefore(ur.getValidTo()))
                .map(UserRole::getRole)
                .filter(r -> r != null && r.getKey() != null)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getKey()))
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser() {
        return user;
    }
}
