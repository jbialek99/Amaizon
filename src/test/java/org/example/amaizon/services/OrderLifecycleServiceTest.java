package org.example.amaizon.services;

import org.example.amaizon.model.entity.*;
import org.example.amaizon.model.enums.OrderStatus;
import org.example.amaizon.model.enums.ShipmentStatus;
import org.example.amaizon.repositories.OrderRepository;
import org.example.amaizon.repositories.ProductStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderLifecycleServiceTest {

    private OrderRepository orders;
    private ProductStockRepository stocks;
    private OrderLifecycleService service;
    private Order order;
    private Shipment shipment;
    private ProductStock stock;
    private UUID stableId;

    @BeforeEach
    void setUp() {
        orders = mock(OrderRepository.class);
        stocks = mock(ProductStockRepository.class);
        service = new OrderLifecycleService(orders, stocks);
        order = mock(Order.class);
        shipment = mock(Shipment.class);
        stock = mock(ProductStock.class);
        stableId = UUID.randomUUID();

        Country country = mock(Country.class);
        Product product = mock(Product.class);
        OrderItem item = mock(OrderItem.class);

        when(country.getId()).thenReturn(1L);
        when(product.getId()).thenReturn(17L);
        when(item.getProduct()).thenReturn(product);
        when(item.getQuantity()).thenReturn(2);
        when(order.getCountry()).thenReturn(country);
        when(order.getItems()).thenReturn(Set.of(item));
        when(order.getShipment()).thenReturn(shipment);
        when(order.getStatus()).thenReturn(OrderStatus.NEW);
        when(shipment.getStatus()).thenReturn(ShipmentStatus.CREATED);

        when(orders.findByStableIdForUpdate(stableId))
                .thenReturn(Optional.of(order));

        when(stocks.findByProductIdAndCountryIdForUpdate(17L, 1L))
                .thenReturn(Optional.of(stock));
    }

    @Test
    void cancellationReleasesReservedStock() {
        service.cancel(stableId);

        verify(stock).release(2);
        verify(shipment).cancel();
        verify(order).cancel("Order cancelled");
        verify(stock, never()).commitReserved(anyInt());
    }

    @Test
    void shippingCommitsReservedStock() {
        service.ship(stableId);

        verify(stock).commitReserved(2);
        verify(shipment).markShipped();
        verify(stock, never()).release(anyInt());
    }

    @Test
    void cancelledOrderCannotBeCancelledAgain() {
        when(order.getStatus()).thenReturn(OrderStatus.CANCELLED);

        assertThrows(
                IllegalStateException.class,
                () -> service.cancel(stableId)
        );

        verifyNoInteractions(stocks);
    }

    @Test
    void shippedOrderCannotBeCancelled() {
        when(order.getStatus()).thenReturn(OrderStatus.SHIPPED);

        assertThrows(
                IllegalStateException.class,
                () -> service.cancel(stableId)
        );

        verifyNoInteractions(stocks);
    }

    @Test
    void shippedOrderCannotBeShippedAgain() {
        when(order.getStatus()).thenReturn(OrderStatus.SHIPPED);

        assertThrows(
                IllegalStateException.class,
                () -> service.ship(stableId)
        );

        verifyNoInteractions(stocks);
    }

    @Test
    void cancelledOrderCannotBeShipped() {
        when(order.getStatus()).thenReturn(OrderStatus.CANCELLED);

        assertThrows(
                IllegalStateException.class,
                () -> service.ship(stableId)
        );

        verifyNoInteractions(stocks);
    }

    @Test
    void missingMarketStockRejectsCancellation() {
        when(stocks.findByProductIdAndCountryIdForUpdate(17L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> service.cancel(stableId)
        );

        verify(shipment, never()).cancel();
        verify(order, never()).cancel(anyString());
    }

    @Test
    void missingOrderRejectsShipping() {
        when(orders.findByStableIdForUpdate(stableId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> service.ship(stableId)
        );

        verifyNoInteractions(stocks);
    }
}
