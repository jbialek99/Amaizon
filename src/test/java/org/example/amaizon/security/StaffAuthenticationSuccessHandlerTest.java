package org.example.amaizon.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.resolvers.MarketResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class StaffAuthenticationSuccessHandlerTest {

    private MarketResolver marketResolver;
    private AuthorizationService authorizationService;
    private StaffAuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        marketResolver = mock(MarketResolver.class);
        authorizationService = mock(AuthorizationService.class);
        handler = new StaffAuthenticationSuccessHandler(
                marketResolver,
                authorizationService
        );
    }

    @Test
    void rejectsUnexpectedPrincipalWithCorrectLoginRedirect() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        MarketContextDto market = mock(MarketContextDto.class, RETURNS_DEEP_STUBS);

        when(marketResolver.resolve(request, response)).thenReturn(market);
        when(market.getCountry().getCode().getValue()).thenReturn("pl");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken("unexpected", null);

        handler.onAuthenticationSuccess(request, response, authentication);

        assertEquals("/home/staff?error=1", response.getRedirectedUrl());
    }

    @Test
    void rejectsUserWithoutMarketPermission() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        MarketContextDto market = mock(MarketContextDto.class, RETURNS_DEEP_STUBS);

        when(marketResolver.resolve(request, response)).thenReturn(market);
        when(market.getCountry().getCode().getValue()).thenReturn("pl");

        User user = mock(User.class);
        CustomUserDetails principal = mock(CustomUserDetails.class);

        when(principal.getUser()).thenReturn(user);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null);

        when(authorizationService.canActInCountry(
                eq(user),
                anyLong()
        )).thenReturn(false);

        handler.onAuthenticationSuccess(request, response, authentication);

        assertEquals("/home/staff?error=1", response.getRedirectedUrl());
    }
}
