package org.example.amaizon.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.entity.Cart;
import org.example.amaizon.model.entity.Country;
import org.example.amaizon.model.entity.ProductCountry;
import org.example.amaizon.repositories.CartRepository;
import org.example.amaizon.repositories.ProductCountryRepository;
import org.example.amaizon.repositories.ProductStockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CartServiceMarketTest {

    @Test
    void shouldRejectProductFromDifferentMarket() {
        CartRepository cartRepository = mock(CartRepository.class);
        ProductCountryRepository productCountryRepository = mock(ProductCountryRepository.class);
        ProductStockRepository productStockRepository = mock(ProductStockRepository.class);

        CartService service = spy(new CartService(
                cartRepository,
                productCountryRepository,
                productStockRepository
        ));

        MarketContextDto market = mock(MarketContextDto.class);
        Country marketCountry = mock(Country.class);
        Country productCountry = mock(Country.class);
        ProductCountry product = mock(ProductCountry.class);
        Cart cart = mock(Cart.class);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        UUID productId = UUID.randomUUID();

        when(market.getCountry()).thenReturn(marketCountry);
        when(marketCountry.getId()).thenReturn(1L);
        when(productCountry.getId()).thenReturn(2L);
        when(product.getCountry()).thenReturn(productCountry);

        when(productCountryRepository.findEntityByProductCountryStableId(productId))
                .thenReturn(Optional.of(product));

        doReturn(cart).when(service).getOrCreateCart(
                eq(market),
                isNull(),
                eq(request),
                eq(response)
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.addToCart(
                        market,
                        null,
                        request,
                        response,
                        productId,
                        1
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(productStockRepository, never()).findByProductCountry(any());
        verify(cart, never()).addProduct(any(), anyInt());
    }
}
