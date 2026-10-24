package org.example.amaizon.services;

import jakarta.persistence.EntityManager;
import org.example.amaizon.model.embeddable.all.AddressSnapshot;
import org.example.amaizon.model.entity.Cart;
import org.example.amaizon.model.entity.Order;
import org.example.amaizon.model.enums.Carrier;
import org.example.amaizon.repositories.CartRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderLifecycleIntegrationTest {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private OrderLifecycleService lifecycleService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @Test
    void cancellationRestoresAvailableStockAndReleasesReservation() {
        TestOrder testOrder = createTestOrder();

        assertEquals(testOrder.availableBefore() - 1, available(testOrder.stockId()));
        assertEquals(testOrder.reservedBefore() + 1, reserved(testOrder.stockId()));

        lifecycleService.cancel(testOrder.stableId());
        entityManager.flush();

        assertEquals(testOrder.availableBefore(), available(testOrder.stockId()));
        assertEquals(testOrder.reservedBefore(), reserved(testOrder.stockId()));

        assertEquals("CANCELLED", orderStatus(testOrder.orderId()));
        assertEquals("CANCELLED", shipmentStatus(testOrder.orderId()));

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.cancel(testOrder.stableId())
        );

        entityManager.flush();

        assertEquals(testOrder.availableBefore(), available(testOrder.stockId()));
        assertEquals(testOrder.reservedBefore(), reserved(testOrder.stockId()));
    }

    @Test
    void shippingConsumesReservedStockExactlyOnce() {
        TestOrder testOrder = createTestOrder();

        Order order = entityManager.find(Order.class, testOrder.orderId());
        order.confirm();
        order.startPayment(null).markSuccess("DEMO-PAYMENT-123456");
        entityManager.flush();

        lifecycleService.ship(testOrder.stableId());
        entityManager.flush();

        assertEquals(testOrder.availableBefore() - 1, available(testOrder.stockId()));
        assertEquals(testOrder.reservedBefore(), reserved(testOrder.stockId()));

        assertEquals("SHIPPED", orderStatus(testOrder.orderId()));
        assertEquals("SHIPPED", shipmentStatus(testOrder.orderId()));

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.ship(testOrder.stableId())
        );

        entityManager.flush();

        assertEquals(testOrder.availableBefore() - 1, available(testOrder.stockId()));
        assertEquals(testOrder.reservedBefore(), reserved(testOrder.stockId()));
    }

    @Test
    void cancelledOrderCannotBeShipped() {
        TestOrder testOrder = createTestOrder();

        lifecycleService.cancel(testOrder.stableId());
        entityManager.flush();

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.ship(testOrder.stableId())
        );

        entityManager.flush();

        assertEquals("CANCELLED", orderStatus(testOrder.orderId()));
        assertEquals("CANCELLED", shipmentStatus(testOrder.orderId()));

        assertEquals(testOrder.availableBefore(), available(testOrder.stockId()));
        assertEquals(testOrder.reservedBefore(), reserved(testOrder.stockId()));
    }

    @Test
    void unpaidOrderCannotBeShippedOrDelivered() {
        TestOrder testOrder = createTestOrder();

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.ship(testOrder.stableId())
        );

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.deliver(testOrder.stableId())
        );

        entityManager.flush();

        assertEquals("NEW", orderStatus(testOrder.orderId()));
        assertEquals("CREATED", shipmentStatus(testOrder.orderId()));
        assertEquals(testOrder.availableBefore() - 1, available(testOrder.stockId()));
        assertEquals(testOrder.reservedBefore() + 1, reserved(testOrder.stockId()));
    }

    @Test
    void confirmedOrderRequiresPaymentBeforeShipping() {
        TestOrder testOrder = createTestOrder();

        Order order = entityManager.find(Order.class, testOrder.orderId());
        order.confirm();
        entityManager.flush();

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.ship(testOrder.stableId())
        );

        assertEquals("CONFIRMED", orderStatus(testOrder.orderId()));
        assertEquals("CREATED", shipmentStatus(testOrder.orderId()));
    }

    @Test
    void completedOrderCannotBeShippedOrDeliveredAgain() {
        TestOrder testOrder = createTestOrder();

        Order order = entityManager.find(Order.class, testOrder.orderId());

        order.confirm();
        order.startPayment(null).markSuccess("DEMO-DELIVERY-123456");
        entityManager.flush();

        lifecycleService.ship(testOrder.stableId());
        lifecycleService.deliver(testOrder.stableId());
        entityManager.flush();

        assertEquals("DELIVERED", orderStatus(testOrder.orderId()));
        assertEquals("DELIVERED", shipmentStatus(testOrder.orderId()));

        assertEquals(testOrder.availableBefore() - 1, available(testOrder.stockId()));
        assertEquals(testOrder.reservedBefore(), reserved(testOrder.stockId()));

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.ship(testOrder.stableId())
        );

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.deliver(testOrder.stableId())
        );
    }

    @Test
    void paidOrderCannotBeCancelledWithoutRefund() {
        TestOrder testOrder = createTestOrder();

        Order order = entityManager.find(Order.class, testOrder.orderId());
        order.confirm();
        order.startPayment(null).markSuccess("DEMO-CANCEL-123456");
        entityManager.flush();

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.cancel(testOrder.stableId())
        );

        assertEquals("PAID", orderStatus(testOrder.orderId()));
        assertEquals("CREATED", shipmentStatus(testOrder.orderId()));
        assertEquals(testOrder.availableBefore() - 1, available(testOrder.stockId()));
        assertEquals(testOrder.reservedBefore() + 1, reserved(testOrder.stockId()));
    }
    @Test
    void failedPaymentCanBeRetriedWithoutChangingStock() {
        TestOrder testOrder = createTestOrder();

        lifecycleService.confirm(testOrder.stableId());
        lifecycleService.beginPayment(testOrder.stableId());
        entityManager.flush();

        assertEquals("CONFIRMED", orderStatus(testOrder.orderId()));

        lifecycleService.failPayment(testOrder.stableId());
        entityManager.flush();

        assertEquals(
                "FAILED",
                paymentStatus(testOrder.orderId())
        );

        assertEquals(
                testOrder.availableBefore() - 1,
                available(testOrder.stockId())
        );

        assertEquals(
                testOrder.reservedBefore() + 1,
                reserved(testOrder.stockId())
        );

        lifecycleService.retryPayment(testOrder.stableId());
        entityManager.flush();

        assertEquals(
                "PENDING",
                paymentStatus(testOrder.orderId())
        );

        assertEquals("CONFIRMED", orderStatus(testOrder.orderId()));
        assertEquals("CREATED", shipmentStatus(testOrder.orderId()));
    }

    @Test
    void failedPaymentOrderCanBeCancelledAndStockReleased() {
        TestOrder testOrder = createTestOrder();

        lifecycleService.confirm(testOrder.stableId());
        lifecycleService.beginPayment(testOrder.stableId());
        lifecycleService.failPayment(testOrder.stableId());

        lifecycleService.cancel(testOrder.stableId());
        entityManager.flush();

        assertEquals("CANCELLED", orderStatus(testOrder.orderId()));
        assertEquals("CANCELLED", shipmentStatus(testOrder.orderId()));
        assertEquals("FAILED", paymentStatus(testOrder.orderId()));

        assertEquals(
                testOrder.availableBefore(),
                available(testOrder.stockId())
        );

        assertEquals(
                testOrder.reservedBefore(),
                reserved(testOrder.stockId())
        );

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.retryPayment(testOrder.stableId())
        );
    }

    private String paymentStatus(long orderId) {
        return jdbc.queryForObject(
                "SELECT status FROM payments WHERE order_id = ?",
                String.class,
                orderId
        );
    }
    @Test
    void cancellingPendingPaymentClosesPaymentAndRestoresStock() {
        TestOrder testOrder = createTestOrder();

        lifecycleService.confirm(testOrder.stableId());
        lifecycleService.beginPayment(testOrder.stableId());
        entityManager.flush();

        assertEquals("PENDING", paymentStatus(testOrder.orderId()));

        lifecycleService.cancel(testOrder.stableId());
        entityManager.flush();

        assertEquals("CANCELLED", orderStatus(testOrder.orderId()));
        assertEquals("CANCELLED", shipmentStatus(testOrder.orderId()));
        assertEquals("FAILED", paymentStatus(testOrder.orderId()));

        assertEquals(
                testOrder.availableBefore(),
                available(testOrder.stockId())
        );

        assertEquals(
                testOrder.reservedBefore(),
                reserved(testOrder.stockId())
        );

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.retryPayment(testOrder.stableId())
        );
    }
    private TestOrder createTestOrder() {
        Long stockId = jdbc.queryForObject(
                """
                SELECT ps.id
                FROM product_stocks ps
                JOIN product_countries pc ON pc.id = ps.product_country_id
                JOIN countries c ON c.id = pc.country_id
                WHERE ps.quantity_available >= 1
                  AND c.code IN ('PL', 'US')
                ORDER BY ps.id
                LIMIT 1
                """,
                Long.class
        );

        assertNotNull(stockId);

        Long productCountryId = jdbc.queryForObject(
                "SELECT product_country_id FROM product_stocks WHERE id = ?",
                Long.class,
                stockId
        );

        assertNotNull(productCountryId);

        Integer availableBefore = available(stockId);
        Integer reservedBefore = reserved(stockId);

        String countryCode = jdbc.queryForObject(
                """
                SELECT c.code
                FROM product_countries pc
                JOIN countries c ON c.id = pc.country_id
                WHERE pc.id = ?
                """,
                String.class,
                productCountryId
        );

        assertNotNull(countryCode);

        UUID cartStableId = UUID.randomUUID();
        UUID guestToken = UUID.randomUUID();

        Long cartId = jdbc.queryForObject(
                """
                INSERT INTO carts (
                    stable_id,
                    guest_token,
                    country_id,
                    currency_id,
                    status,
                    created_at,
                    updated_at
                )
                SELECT ?, ?, pc.country_id, c.currency_id,
                       'ACTIVE', NOW(), NOW()
                FROM product_countries pc
                JOIN countries c ON c.id = pc.country_id
                WHERE pc.id = ?
                RETURNING id
                """,
                Long.class,
                cartStableId,
                guestToken,
                productCountryId
        );

        assertNotNull(cartId);

        assertEquals(
                1,
                jdbc.update(
                        """
                        INSERT INTO cart_items (
                            stable_id,
                            cart_id,
                            product_country_id,
                            quantity,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, 1, NOW(), NOW())
                        """,
                        UUID.randomUUID(),
                        cartId,
                        productCountryId
                )
        );

        entityManager.clear();

        Cart cart = cartRepository.findById(cartId).orElseThrow();

        String countryName = countryCode.equals("US")
                ? "United States"
                : "Poland";

        AddressSnapshot address = new AddressSnapshot(
                "Integration Customer",
                "+48123456789",
                countryCode,
                countryName,
                countryCode.equals("US") ? org.example.amaizon.model.enums.UsState.NY : null,
                countryCode.equals("US") ? "New York" : "Warsaw",
                countryCode.equals("US") ? "10001" : "00-001",
                "Integration Street 1",
                null
        );

        Order order = checkoutService.checkout(
                cart,
                null,
                "lifecycle-" + UUID.randomUUID() + "@example.com",
                address,
                address,
                null,
                null,
                null,
                Carrier.DPD
        );

        entityManager.flush();

        assertEquals(availableBefore - 1, available(stockId));
        assertEquals(reservedBefore + 1, reserved(stockId));

        return new TestOrder(
                order.getId(),
                order.getStableId(),
                stockId,
                availableBefore,
                reservedBefore
        );
    }

    private int available(long stockId) {
        return jdbc.queryForObject(
                "SELECT quantity_available FROM product_stocks WHERE id = ?",
                Integer.class,
                stockId
        );
    }

    private int reserved(long stockId) {
        return jdbc.queryForObject(
                "SELECT quantity_reserved FROM product_stocks WHERE id = ?",
                Integer.class,
                stockId
        );
    }

    private String orderStatus(long orderId) {
        return jdbc.queryForObject(
                "SELECT status FROM orders WHERE id = ?",
                String.class,
                orderId
        );
    }

    private String shipmentStatus(long orderId) {
        return jdbc.queryForObject(
                "SELECT status FROM shipments WHERE order_id = ?",
                String.class,
                orderId
        );
    }

    private record TestOrder(
            long orderId,
            UUID stableId,
            long stockId,
            int availableBefore,
            int reservedBefore
    ) {
    }
}
