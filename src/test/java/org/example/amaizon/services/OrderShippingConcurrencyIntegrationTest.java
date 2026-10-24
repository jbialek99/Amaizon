package org.example.amaizon.services;

import org.example.amaizon.model.embeddable.all.AddressSnapshot;
import org.example.amaizon.model.entity.Cart;
import org.example.amaizon.model.entity.Order;
import org.example.amaizon.model.enums.Carrier;
import org.example.amaizon.repositories.CartRepository;
import org.example.amaizon.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderShippingConcurrencyIntegrationTest {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private OrderLifecycleService lifecycleService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate transactions;

    @Test
    void concurrentShippingCommitsInventoryOnlyOnce() throws Exception {
        Long stockId = jdbc.queryForObject(
                """
                SELECT ps.id
                FROM product_stocks ps
                JOIN product_countries pc ON pc.id = ps.product_country_id
                JOIN countries c ON c.id = pc.country_id
                WHERE c.code = 'PL'
                  AND ps.quantity_available >= 1
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

        int availableBefore = available(stockId);
        int reservedBefore = reserved(stockId);

        Long cartId = null;
        Long orderId = null;
        UUID orderStableId = null;

        try {
            cartId = jdbc.queryForObject(
                    """
                    INSERT INTO carts (
                        stable_id, guest_token, country_id,
                        currency_id, status, created_at, updated_at
                    )
                    SELECT ?, ?, pc.country_id, c.currency_id,
                           'ACTIVE', NOW(), NOW()
                    FROM product_countries pc
                    JOIN countries c ON c.id = pc.country_id
                    WHERE pc.id = ?
                    RETURNING id
                    """,
                    Long.class,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    productCountryId
            );

            assertNotNull(cartId);

            assertEquals(
                    1,
                    jdbc.update(
                            """
                            INSERT INTO cart_items (
                                stable_id, cart_id, product_country_id,
                                quantity, created_at, updated_at
                            )
                            VALUES (?, ?, ?, 1, NOW(), NOW())
                            """,
                            UUID.randomUUID(),
                            cartId,
                            productCountryId
                    )
            );

            Long finalCartId = cartId;

            AddressSnapshot address = new AddressSnapshot(
                    "Concurrency Customer",
                    "+48123456789",
                    "PL",
                    "Poland",
                    null,
                    "Warsaw",
                    "00-001",
                    "Concurrency Street 1",
                    null
            );

            UUID createdOrder = transactions.execute(status -> {
                Cart cart = cartRepository.findById(finalCartId).orElseThrow();

                Order order = checkoutService.checkout(
                        cart,
                        null,
                        "shipping-race-" + UUID.randomUUID() + "@example.com",
                        address,
                        address,
                        null,
                        null,
                        null,
                        Carrier.DPD
                );

                order.confirm();
                order.startPayment(null).markSuccess("DEMO-RACE-" + UUID.randomUUID());

                return order.getStableId();
            });

            assertNotNull(createdOrder);
            orderStableId = createdOrder;

            orderId = jdbc.queryForObject(
                    "SELECT id FROM orders WHERE stable_id = ?",
                    Long.class,
                    createdOrder
            );

            assertNotNull(orderId);
            assertEquals(availableBefore - 1, available(stockId));
            assertEquals(reservedBefore + 1, reserved(stockId));

            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService executor = Executors.newFixedThreadPool(2);

            try {
                UUID finalOrderStableId = createdOrder;

                Future<Boolean> first = executor.submit(
                        () -> attemptShipping(finalOrderStableId, ready, start)
                );

                Future<Boolean> second = executor.submit(
                        () -> attemptShipping(finalOrderStableId, ready, start)
                );

                assertTrue(ready.await(15, TimeUnit.SECONDS));
                start.countDown();

                boolean firstResult = first.get(45, TimeUnit.SECONDS);
                boolean secondResult = second.get(45, TimeUnit.SECONDS);

                int successful = (firstResult ? 1 : 0) + (secondResult ? 1 : 0);

                assertEquals(1, successful);
            } finally {
                start.countDown();
                executor.shutdownNow();
                assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
            }

            assertEquals(
                    "SHIPPED",
                    jdbc.queryForObject(
                            "SELECT status FROM orders WHERE id = ?",
                            String.class,
                            orderId
                    )
            );

            assertEquals(
                    "SHIPPED",
                    jdbc.queryForObject(
                            "SELECT status FROM shipments WHERE order_id = ?",
                            String.class,
                            orderId
                    )
            );

            assertEquals(availableBefore - 1, available(stockId));
            assertEquals(reservedBefore, reserved(stockId));

            System.out.println("=== SHIPPING CONCURRENCY RESULT ===");
            System.out.println("Successful shipments: 1");
            System.out.println("Rejected shipments: 1");
            System.out.println("Stock committed exactly once: true");
        } finally {
            if (cartId != null) {
                Long finalCartId = cartId;
                Long finalOrderId = orderId;

                transactions.executeWithoutResult(status -> {
                    Integer lockedAvailable = jdbc.queryForObject(
                            """
                            SELECT quantity_available
                            FROM product_stocks
                            WHERE id = ?
                            FOR UPDATE
                            """,
                            Integer.class,
                            stockId
                    );

                    Integer lockedReserved = jdbc.queryForObject(
                            "SELECT quantity_reserved FROM product_stocks WHERE id = ?",
                            Integer.class,
                            stockId
                    );

                    if (lockedAvailable == null || lockedReserved == null) {
                        throw new IllegalStateException("Test stock missing");
                    }

                    if (finalOrderId != null) {
                        if (lockedAvailable != availableBefore - 1
                                || (lockedReserved != reservedBefore
                                && lockedReserved != reservedBefore + 1)) {
                            throw new IllegalStateException(
                                    "Unexpected inventory state; refusing cleanup"
                            );
                        }

                        jdbc.update(
                                "DELETE FROM payments WHERE order_id = ?",
                                finalOrderId
                        );

                        jdbc.update(
                                "DELETE FROM shipments WHERE order_id = ?",
                                finalOrderId
                        );

                        jdbc.update(
                                "DELETE FROM order_items WHERE order_id = ?",
                                finalOrderId
                        );

                        if (jdbc.update(
                                "DELETE FROM orders WHERE id = ?",
                                finalOrderId
                        ) != 1) {
                            throw new IllegalStateException("Test order cleanup failed");
                        }

                        int reservedToRelease = lockedReserved - reservedBefore;

                        if (jdbc.update(
                                """
                                UPDATE product_stocks
                                SET quantity_available = quantity_available + 1,
                                    quantity_reserved = quantity_reserved - ?
                                WHERE id = ?
                                """,
                                reservedToRelease,
                                stockId
                        ) != 1) {
                            throw new IllegalStateException("Stock cleanup failed");
                        }
                    } else if (lockedAvailable != availableBefore
                            || lockedReserved != reservedBefore) {
                        throw new IllegalStateException(
                                "Incomplete test setup; refusing unsafe cleanup"
                        );
                    }

                    jdbc.update(
                            "DELETE FROM cart_items WHERE cart_id = ?",
                            finalCartId
                    );

                    if (jdbc.update(
                            "DELETE FROM carts WHERE id = ?",
                            finalCartId
                    ) != 1) {
                        throw new IllegalStateException("Test cart cleanup failed");
                    }
                });

                assertEquals(availableBefore, available(stockId));
                assertEquals(reservedBefore, reserved(stockId));

                System.out.println("=== SHIPPING CONCURRENCY CLEANUP ===");
                System.out.println("Stock restored: true");
                System.out.println("Test order and cart removed: true");
            }
        }
    }

    private boolean attemptShipping(
            UUID stableId,
            CountDownLatch ready,
            CountDownLatch start
    ) throws Exception {
        ready.countDown();

        if (!start.await(15, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Start barrier timeout");
        }

        try {
            lifecycleService.ship(stableId);
            return true;
        } catch (IllegalStateException exception) {
            if ("Only paid orders can be shipped".equals(exception.getMessage())) {
                return false;
            }

            throw exception;
        }
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
}
