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
        Payment payment = mock(Payment.class);
        when(order.getStatus()).thenReturn(OrderStatus.PAID);
        when(order.getPayment()).thenReturn(payment);
        when(payment.getStatus()).thenReturn(org.example.amaizon.model.enums.PaymentStatus.SUCCESS);

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
    @Test
    void unpaidOrderCannotBeShipped() {
        assertThrows(
                IllegalStateException.class,
                () -> service.ship(stableId)
        );

        verifyNoInteractions(stocks);
        verify(shipment, never()).markShipped();
    }

    @Test
    void paidOrderCannotBeCancelled() {
        when(order.getStatus()).thenReturn(OrderStatus.PAID);

        assertThrows(
                IllegalStateException.class,
                () -> service.cancel(stableId)
        );

        verifyNoInteractions(stocks);
        verify(shipment, never()).cancel();
    }

    @Test
    void shippedOrderCanBeDelivered() {
        when(order.getStatus()).thenReturn(OrderStatus.SHIPPED);
        when(shipment.getStatus()).thenReturn(ShipmentStatus.SHIPPED);

        service.deliver(stableId);

        verify(shipment).markDelivered();
        verifyNoInteractions(stocks);
    }

    @Test
    void unpaidOrderCannotBeDelivered() {
        assertThrows(
                IllegalStateException.class,
                () -> service.deliver(stableId)
        );

        verify(shipment, never()).markDelivered();
        verifyNoInteractions(stocks);
    }

    @Test
    void deliveredOrderCannotBeDeliveredAgain() {
        when(order.getStatus()).thenReturn(OrderStatus.DELIVERED);

        assertThrows(
                IllegalStateException.class,
                () -> service.deliver(stableId)
        );

        verify(shipment, never()).markDelivered();
        verifyNoInteractions(stocks);
    }

    @Test
    void confirmingNewOrderDelegatesToDomain() {
        service.confirm(stableId);
        verify(order).confirm();
    }

    @Test
    void beginPaymentRequiresConfirmedOrder() {
        assertThrows(
                IllegalStateException.class,
                () -> service.beginPayment(stableId)
        );
        verify(order, never()).startPayment(any());
    }

    @Test
    void confirmedOrderCanBeginPayment() {
        when(order.getStatus()).thenReturn(OrderStatus.CONFIRMED);

        service.beginPayment(stableId);

        verify(order).startPayment(null);
    }

    @Test
    void failedPaymentCanBeRecorded() {
        Payment payment = mock(Payment.class);
        when(order.getStatus()).thenReturn(OrderStatus.CONFIRMED);
        when(order.getPayment()).thenReturn(payment);
        when(payment.getStatus()).thenReturn(org.example.amaizon.model.enums.PaymentStatus.PENDING);

        service.failPayment(stableId);

        verify(payment).markFailed("Payment unsuccessful");
        verifyNoInteractions(stocks);
    }

    @Test
    void successfulPaymentCannotBeMarkedFailed() {
        Payment payment = mock(Payment.class);
        when(order.getStatus()).thenReturn(OrderStatus.CONFIRMED);
        when(order.getPayment()).thenReturn(payment);
        when(payment.getStatus()).thenReturn(org.example.amaizon.model.enums.PaymentStatus.SUCCESS);

        assertThrows(
                IllegalStateException.class,
                () -> service.failPayment(stableId)
        );

        verify(payment, never()).markFailed(anyString());
    }

    @Test
    void failedPaymentCanBeRetried() {
        Payment payment = mock(Payment.class);
        when(order.getStatus()).thenReturn(OrderStatus.CONFIRMED);
        when(order.getPayment()).thenReturn(payment);
        when(payment.getStatus()).thenReturn(org.example.amaizon.model.enums.PaymentStatus.FAILED);

        service.retryPayment(stableId);

        verify(payment).retry();
        verifyNoInteractions(stocks);
    }

    @Test
    void pendingPaymentCannotBeRetried() {
        Payment payment = mock(Payment.class);
        when(order.getStatus()).thenReturn(OrderStatus.CONFIRMED);
        when(order.getPayment()).thenReturn(payment);
        when(payment.getStatus()).thenReturn(org.example.amaizon.model.enums.PaymentStatus.PENDING);

        assertThrows(
                IllegalStateException.class,
                () -> service.retryPayment(stableId)
        );

        verify(payment, never()).retry();
    }

    @Test
    void cancellingPendingPaymentMarksItFailed() {
        Payment payment = mock(Payment.class);

        when(order.getPayment()).thenReturn(payment);
        when(payment.getStatus()).thenReturn(
                org.example.amaizon.model.enums.PaymentStatus.PENDING
        );

        service.cancel(stableId);

        verify(stock).release(2);
        verify(payment).markFailed("Order cancelled");
        verify(shipment).cancel();
        verify(order).cancel("Order cancelled");
    }

}
