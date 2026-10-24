package org.example.amaizon.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class StaffSecurityModelAdvice {

    @ModelAttribute("isHeadAdmin")
    public boolean isHeadAdmin() {
        return hasRole("HEAD_ADMIN");
    }

    @ModelAttribute("isCountryAdmin")
    public boolean isCountryAdmin() {
        return hasRole("COUNTRY_ADMIN");
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;

        String expected = "ROLE_" + role;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (expected.equals(ga.getAuthority())) return true;
        }
        return false;
    }
}
