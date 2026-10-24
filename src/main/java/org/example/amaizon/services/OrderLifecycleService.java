package org.example.amaizon.services;

import org.example.amaizon.model.entity.Order;
import org.example.amaizon.model.entity.OrderItem;
import org.example.amaizon.model.entity.ProductStock;
import org.example.amaizon.model.entity.Shipment;
import org.example.amaizon.model.enums.OrderStatus;
import org.example.amaizon.model.enums.ShipmentStatus;
import org.example.amaizon.repositories.OrderRepository;
import org.example.amaizon.repositories.ProductStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class OrderLifecycleService {

    private final OrderRepository orderRepository;
    private final ProductStockRepository stockRepository;

    public OrderLifecycleService(
            OrderRepository orderRepository,
            ProductStockRepository stockRepository
    ) {
        this.orderRepository = orderRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void cancel(UUID stableId) {
        Order order = loadLocked(stableId);

        if (order.getStatus() != OrderStatus.NEW
                && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order cannot be cancelled");
        }

        if (order.getPayment() != null
                && order.getPayment().getStatus() == org.example.amaizon.model.enums.PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Paid order cannot be cancelled without refund");
        }

        Shipment shipment = order.getShipment();

        if (shipment == null || shipment.getStatus() != ShipmentStatus.CREATED) {
            throw new IllegalStateException("Shipment cannot be cancelled");
        }

        for (OrderItem item : orderedItems(order)) {
            lockStock(order, item).release(item.getQuantity());
        }

        shipment.cancel();
        order.cancel("Order cancelled");
    }

    @Transactional
    public void ship(UUID stableId) {
        Order order = loadLocked(stableId);

        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Only paid orders can be shipped");
        }

        if (order.getPayment() == null
                || order.getPayment().getStatus() != org.example.amaizon.model.enums.PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Successful payment required before shipping");
        }

        Shipment shipment = order.getShipment();

        if (shipment == null || shipment.getStatus() != ShipmentStatus.CREATED) {
            throw new IllegalStateException("Shipment cannot be shipped");
        }

        for (OrderItem item : orderedItems(order)) {
            lockStock(order, item).commitReserved(item.getQuantity());
        }

        shipment.markShipped();
    }

    @Transactional
    public void deliver(UUID stableId) {
        Order order = loadLocked(stableId);

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Only shipped orders can be delivered");
        }

        Shipment shipment = order.getShipment();

        if (shipment == null || shipment.getStatus() != ShipmentStatus.SHIPPED) {
            throw new IllegalStateException("Shipment is not ready for delivery");
        }

        shipment.markDelivered();
    }
    private Order loadLocked(UUID stableId) {
        if (stableId == null) {
            throw new IllegalArgumentException("Order identifier required");
        }

        return orderRepository.findByStableIdForUpdate(stableId)
                .orElseThrow(() -> new IllegalStateException("Order not found"));
    }

    private List<OrderItem> orderedItems(Order order) {
        List<OrderItem> items = order.getItems().stream()
                .sorted(Comparator.comparingLong(
                        item -> item.getProduct().getId()
                ))
                .toList();

        if (items.isEmpty()) {
            throw new IllegalStateException("Order has no items");
        }

        for (OrderItem item : items) {
            if (item.getProduct() == null || item.getQuantity() <= 0) {
                throw new IllegalStateException("Invalid order item");
            }
        }

        return items;
    }

    private ProductStock lockStock(Order order, OrderItem item) {
        if (order.getCountry() == null) {
            throw new IllegalStateException("Order market missing");
        }

        return stockRepository.findByProductIdAndCountryIdForUpdate(
                        item.getProduct().getId(),
                        order.getCountry().getId()
                )
                .orElseThrow(() ->
                        new IllegalStateException("Market stock not found")
                );
    }
}
