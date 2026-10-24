package org.example.amaizon.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.amaizon.model.entity.Order;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.repositories.OrderRepository;
import org.example.amaizon.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.AbstractView;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class OrdersHttpSecurityTest {

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mvc;
    private OrderRepository repository;
    private Order order;
    private UUID stableId;

    @BeforeEach
    void setUp() {
        repository = mock(OrderRepository.class);
        order = mock(Order.class);
        stableId = UUID.randomUUID();

        when(order.getStableId()).thenReturn(stableId);
        when(repository.findDetailsByStableId(stableId))
                .thenReturn(Optional.of(order));

        mvc = MockMvcBuilders
                .standaloneSetup(new OrdersController(repository))
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver()
                )
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
    void anonymousWithoutGrantReceives404() throws Exception {
        mvc.perform(get("/orders/{stableId}", stableId))
                .andExpect(status().isNotFound());
    }

    @Test
    void guestWithValidCheckoutSessionReceives200() throws Exception {
        MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                "AMAIZON_GUEST_ORDER_" + stableId,
                Boolean.TRUE
        );

        mvc.perform(get("/orders/{stableId}", stableId).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/details"))
                .andExpect(model().attribute("order", order));
    }

    @Test
    void guestWithGrantCannotReadRegisteredOwnersOrder() throws Exception {
        User owner = mock(User.class);
        when(order.getUser()).thenReturn(owner);

        MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                "AMAIZON_GUEST_ORDER_" + stableId,
                Boolean.TRUE
        );

        mvc.perform(get("/orders/{stableId}", stableId).session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void authenticatedOwnerReceives200() throws Exception {
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(101L);
        when(order.getUser()).thenReturn(owner);

        mvc.perform(
                        get("/orders/{stableId}", stableId)
                                .with(authenticationForUser(101L))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("orders/details"))
                .andExpect(model().attribute("order", order));
    }

    @Test
    void authenticatedNonOwnerReceives404() throws Exception {
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(101L);
        when(order.getUser()).thenReturn(owner);

        mvc.perform(
                        get("/orders/{stableId}", stableId)
                                .with(authenticationForUser(202L))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void missingOrderReceives404() throws Exception {
        UUID missingId = UUID.randomUUID();

        when(repository.findDetailsByStableId(missingId))
                .thenReturn(Optional.empty());

        mvc.perform(get("/orders/{stableId}", missingId))
                .andExpect(status().isNotFound());
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor
    authenticationForUser(long userId) {
        User user = mock(User.class);

        when(user.getId()).thenReturn(userId);

        CustomUserDetails details = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        details,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        return authentication(authentication);
    }
}
