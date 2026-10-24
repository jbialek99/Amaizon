package org.example.amaizon.services;

import org.example.amaizon.model.entity.Cart;
import org.example.amaizon.model.enums.CartStatus;
import org.example.amaizon.model.enums.Carrier;
import org.example.amaizon.repositories.CartRepository;
import org.example.amaizon.repositories.OrderRepository;
import org.example.amaizon.repositories.ProductStockRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CheckoutServiceSafetyTest {

    private final CartRepository cartRepository = mock(CartRepository.class);
    private final ProductStockRepository stockRepository = mock(ProductStockRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);

    private final CheckoutService service = new CheckoutService(
            cartRepository,
            stockRepository,
            orderRepository
    );

    @Test
    void rejectsAlreadyCheckedOutCart() {
        Cart cart = mock(Cart.class);

        when(cart.getId()).thenReturn(10L);
        when(cartRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(cart));
        when(cart.getStatus()).thenReturn(CartStatus.CHECKED_OUT);

        assertThrows(IllegalStateException.class, () -> checkout(cart));

        verifyNoInteractions(stockRepository, orderRepository);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void rejectsEmptyActiveCart() {
        Cart cart = mock(Cart.class);

        when(cart.getId()).thenReturn(11L);
        when(cartRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(cart));
        when(cart.getStatus()).thenReturn(CartStatus.ACTIVE);
        when(cart.getItems()).thenReturn(Set.of());

        assertThrows(IllegalStateException.class, () -> checkout(cart));

        verifyNoInteractions(stockRepository, orderRepository);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void rejectsMissingPersistedCart() {
        Cart cart = mock(Cart.class);

        when(cart.getId()).thenReturn(12L);
        when(cartRepository.findByIdForUpdate(12L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> checkout(cart));

        verifyNoInteractions(stockRepository, orderRepository);
    }

    private void checkout(Cart cart) {
        service.checkout(
                cart,
                null,
                "test@example.com",
                mock(org.example.amaizon.model.embeddable.all.AddressSnapshot.class),
                mock(org.example.amaizon.model.embeddable.all.AddressSnapshot.class),
                null,
                null,
                null,
                Carrier.DPD
        );
    }
}
