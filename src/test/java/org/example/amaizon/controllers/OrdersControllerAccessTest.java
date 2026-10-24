package org.example.amaizon.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.example.amaizon.model.entity.Order;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.repositories.OrderRepository;
import org.example.amaizon.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrdersControllerAccessTest {

    private OrderRepository repository;
    private OrdersController controller;
    private UUID stableId;
    private Order order;
    private MockHttpServletRequest request;
    private ExtendedModelMap model;

    @BeforeEach
    void setUp() {
        repository = mock(OrderRepository.class);
        controller = new OrdersController(repository);
        stableId = UUID.randomUUID();
        order = mock(Order.class);
        request = new MockHttpServletRequest();
        model = new ExtendedModelMap();

        when(order.getStableId()).thenReturn(stableId);
        when(repository.findDetailsByStableId(stableId))
                .thenReturn(Optional.of(order));
    }

    @Test
    void anonymousUserCannotReadOrderWithoutSessionGrant() {
        assertNotFound(null);
    }

    @Test
    void guestCanReadOwnOrderInCheckoutSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                "AMAIZON_GUEST_ORDER_" + stableId,
                Boolean.TRUE
        );
        request.setSession(session);

        String view = controller.details(
                stableId,
                model,
                request,
                null
        );

        assertEquals("orders/details", view);
        assertSame(order, model.get("order"));
    }

    @Test
    void guestCannotReadAnotherCustomersOrder() {
        User owner = mock(User.class);
        when(order.getUser()).thenReturn(owner);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                "AMAIZON_GUEST_ORDER_" + stableId,
                Boolean.TRUE
        );
        request.setSession(session);

        assertNotFound(null);
    }

    @Test
    void authenticatedOwnerCanReadOwnOrder() {
        User owner = mock(User.class);
        User currentUser = mock(User.class);

        when(owner.getId()).thenReturn(10L);
        when(currentUser.getId()).thenReturn(10L);
        when(order.getUser()).thenReturn(owner);

        CustomUserDetails principal = mock(CustomUserDetails.class);
        when(principal.getUser()).thenReturn(currentUser);

        String view = controller.details(
                stableId,
                model,
                request,
                principal
        );

        assertEquals("orders/details", view);
        assertSame(order, model.get("order"));
    }

    @Test
    void authenticatedUserCannotReadAnotherUsersOrder() {
        User owner = mock(User.class);
        User currentUser = mock(User.class);

        when(owner.getId()).thenReturn(10L);
        when(currentUser.getId()).thenReturn(20L);
        when(order.getUser()).thenReturn(owner);

        CustomUserDetails principal = mock(CustomUserDetails.class);
        when(principal.getUser()).thenReturn(currentUser);

        assertNotFound(principal);
    }

    private void assertNotFound(CustomUserDetails principal) {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.details(
                        stableId,
                        model,
                        request,
                        principal
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertFalse(model.containsAttribute("order"));
    }
}
