package org.example.amaizon.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.example.amaizon.security.CustomUserDetails;
import org.example.amaizon.services.StaffProductAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.AbstractView;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class StaffProductsHttpSecurityTest {

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private StaffProductsController controller;

    @MockitoBean
    private MarketResolver marketResolver;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private StaffProductAdminService staffProductAdminService;

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        reset(marketResolver, userRepository, staffProductAdminService);

        when(marketResolver.resolve(any(), any())).thenAnswer(invocation ->
                market(1L)
        );

        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setViewResolvers((viewName, locale) -> new AbstractView() {
                    @Override
                    protected void renderMergedOutputModel(
                            Map<String, Object> model,
                            HttpServletRequest request,
                            HttpServletResponse response
                    ) {
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                })
                .apply(springSecurity(springSecurityFilterChain))
                .build();
    }

    @Test
    void anonymousStaffRequestRedirectsToStaffLogin() throws Exception {
        mvc.perform(get("/staff/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/home/staff"));
    }

    @Test
    void headAdminCanAccessProductsMenu() throws Exception {
        User headAdmin = userWithRole(RoleKey.HEAD_ADMIN, null);
        when(userRepository.findWithRolesByEmail("staff@example.com"))
                .thenReturn(Optional.of(headAdmin));

        mvc.perform(get("/staff/products")
                        .with(authenticationFor(headAdmin)))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/products"));
    }

    @Test
    void sellerCannotAccessProductsMenu() throws Exception {
        User seller = userWithRole(RoleKey.SELLER, 1L);
        when(userRepository.findWithRolesByEmail("staff@example.com"))
                .thenReturn(Optional.of(seller));

        mvc.perform(get("/staff/products")
                        .with(authenticationFor(seller)))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorFromDifferentMarketReceives403() throws Exception {
        User polishAdmin = userWithRole(RoleKey.COUNTRY_ADMIN, 1L);
        User freshUser = userWithRole(RoleKey.COUNTRY_ADMIN, 2L);

        when(userRepository.findWithRolesByEmail("staff@example.com"))
                .thenReturn(Optional.of(freshUser));

        mvc.perform(get("/staff/products")
                        .with(authenticationFor(polishAdmin)))
                .andExpect(status().isForbidden());
    }

    @Test
    void downgradedAdministratorCannotReuseOldAuthorities() throws Exception {
        User originalAdmin = userWithRole(RoleKey.COUNTRY_ADMIN, 1L);
        User downgraded = userWithRole(RoleKey.SELLER, 1L);

        when(userRepository.findWithRolesByEmail("staff@example.com"))
                .thenReturn(Optional.of(downgraded));

        mvc.perform(get("/staff/products")
                        .with(authenticationFor(originalAdmin)))
                .andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor
    authenticationFor(User user) {
        CustomUserDetails details = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        details,
                        null,
                        List.copyOf(details.getAuthorities())
                );

        return authentication(authentication);
    }

    private User userWithRole(RoleKey key, Long countryId) {
        User user = mock(User.class);
        UserRole assignment = mock(UserRole.class);
        Role role = mock(Role.class);

        when(user.getEmail()).thenReturn("staff@example.com");
        when(user.getUserRoles()).thenReturn(Set.of(assignment));

        when(assignment.getStatus()).thenReturn(UserRoleStatus.ACTIVE);
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

    private MarketContextDto market(long countryId) {
        MarketContextDto market = mock(MarketContextDto.class);
        Country country = mock(Country.class);

        when(country.getId()).thenReturn(countryId);
        when(market.getCountry()).thenReturn(country);

        return market;
    }
}
