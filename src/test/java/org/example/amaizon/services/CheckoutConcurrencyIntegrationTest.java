package org.example.amaizon.services;

import org.example.amaizon.model.embeddable.all.AddressSnapshot;
import org.example.amaizon.model.entity.Cart;
import org.example.amaizon.model.enums.Carrier;
import org.example.amaizon.repositories.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CheckoutConcurrencyIntegrationTest {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long cleanupCartId;
    private Long cleanupProductCountryId;
    private String cleanupEmail;
    private Integer cleanupAvailableBefore;
    private Integer cleanupReservedBefore;

    @Test
    void concurrentCheckoutCreatesExactlyOneOrder() throws Exception {
        Long productCountryId = jdbcTemplate.queryForObject(
                "SELECT id FROM product_countries ORDER BY id LIMIT 1",
                Long.class
        );

        Integer availableBefore = jdbcTemplate.queryForObject(
                "SELECT quantity_available FROM product_stocks WHERE product_country_id = ?",
                Integer.class,
                productCountryId
        );

        Integer reservedBefore = jdbcTemplate.queryForObject(
                "SELECT quantity_reserved FROM product_stocks WHERE product_country_id = ?",
                Integer.class,
                productCountryId
        );

        assertTrue(availableBefore != null && availableBefore >= 1);

        cleanupProductCountryId = productCountryId;
        cleanupAvailableBefore = availableBefore;
        cleanupReservedBefore = reservedBefore;

        UUID cartStableId = UUID.randomUUID();
        UUID guestToken = UUID.randomUUID();
        String contactEmail = "race-" + UUID.randomUUID() + "@example.com";
        cleanupEmail = contactEmail;

        Long cartId = jdbcTemplate.queryForObject(
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

        assertTrue(cartId != null && cartId > 0);
        cleanupCartId = cartId;

        int insertedItems = jdbcTemplate.update(
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
        );

        assertEquals(1, insertedItems);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        int successful = 0;
        int rejected = 0;

        try {
            Future<Boolean> first = executor.submit(() ->
                    attemptCheckout(cartId, contactEmail, ready, start)
            );

            Future<Boolean> second = executor.submit(() ->
                    attemptCheckout(cartId, contactEmail, ready, start)
            );

            assertTrue(ready.await(15, TimeUnit.SECONDS));

            start.countDown();

            boolean firstResult = first.get(45, TimeUnit.SECONDS);
            boolean secondResult = second.get(45, TimeUnit.SECONDS);

            successful = (firstResult ? 1 : 0) + (secondResult ? 1 : 0);
            rejected = (firstResult ? 0 : 1) + (secondResult ? 0 : 1);
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
        }

        Long orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE contact_email = ?",
                Long.class,
                contactEmail
        );

        Long orderItemCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM order_items oi
                JOIN orders o ON o.id = oi.order_id
                WHERE o.contact_email = ?
                """,
                Long.class,
                contactEmail
        );

        String cartStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM carts WHERE id = ?",
                String.class,
                cartId
        );

        Integer availableAfter = jdbcTemplate.queryForObject(
                "SELECT quantity_available FROM product_stocks WHERE product_country_id = ?",
                Integer.class,
                productCountryId
        );

        Integer reservedAfter = jdbcTemplate.queryForObject(
                "SELECT quantity_reserved FROM product_stocks WHERE product_country_id = ?",
                Integer.class,
                productCountryId
        );

        System.out.println("=== CHECKOUT CONCURRENCY RESULT ===");
        System.out.println("Successful checkouts: " + successful);
        System.out.println("Rejected checkouts: " + rejected);
        System.out.println("Orders created: " + orderCount);
        System.out.println("Order items created: " + orderItemCount);
        System.out.println("Cart status: " + cartStatus);
        System.out.println("Available stock before: " + availableBefore);
        System.out.println("Available stock after: " + availableAfter);
        System.out.println("Reserved stock before: " + reservedBefore);
        System.out.println("Reserved stock after: " + reservedAfter);

        assertEquals(1, successful);
        assertEquals(1, rejected);
        assertEquals(1L, orderCount);
        assertEquals(1L, orderItemCount);
        assertEquals("CHECKED_OUT", cartStatus);
        assertEquals(availableBefore - 1, availableAfter);
        assertEquals(reservedBefore + 1, reservedAfter);
    }

    @AfterEach
    void cleanupTestFixtures() {
        if (cleanupCartId == null) {
            return;
        }

        transactionTemplate.executeWithoutResult(transaction -> {
            Long orderCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM orders WHERE contact_email = ?",
                    Long.class,
                    cleanupEmail
            );

            if (orderCount == null || orderCount > 1) {
                throw new IllegalStateException("Unexpected test order count");
            }

            Integer currentAvailable = jdbcTemplate.queryForObject(
                    """
                    SELECT quantity_available
                    FROM product_stocks
                    WHERE product_country_id = ?
                    FOR UPDATE
                    """,
                    Integer.class,
                    cleanupProductCountryId
            );

            Integer currentReserved = jdbcTemplate.queryForObject(
                    """
                    SELECT quantity_reserved
                    FROM product_stocks
                    WHERE product_country_id = ?
                    """,
                    Integer.class,
                    cleanupProductCountryId
            );

            int expectedAvailable = cleanupAvailableBefore - orderCount.intValue();
            int expectedReserved = cleanupReservedBefore + orderCount.intValue();

            if (currentAvailable == null
                    || currentReserved == null
                    || currentAvailable != expectedAvailable
                    || currentReserved != expectedReserved) {
                throw new IllegalStateException(
                        "Stock changed unexpectedly; refusing cleanup"
                );
            }

            if (orderCount == 1) {
                Long orderId = jdbcTemplate.queryForObject(
                        "SELECT id FROM orders WHERE contact_email = ?",
                        Long.class,
                        cleanupEmail
                );

                if (orderId == null) {
                    throw new IllegalStateException("Test order missing");
                }

                jdbcTemplate.update(
                        "DELETE FROM order_items WHERE order_id = ?",
                        orderId
                );

                jdbcTemplate.update(
                        "DELETE FROM shipments WHERE order_id = ?",
                        orderId
                );

                if (jdbcTemplate.update(
                        "DELETE FROM orders WHERE id = ?",
                        orderId
                ) != 1) {
                    throw new IllegalStateException("Test order cleanup failed");
                }

                if (jdbcTemplate.update(
                        """
                        UPDATE product_stocks
                        SET quantity_available = quantity_available + 1,
                            quantity_reserved = quantity_reserved - 1
                        WHERE product_country_id = ?
                          AND quantity_reserved >= 1
                        """,
                        cleanupProductCountryId
                ) != 1) {
                    throw new IllegalStateException("Stock cleanup failed");
                }
            }

            jdbcTemplate.update(
                    "DELETE FROM cart_items WHERE cart_id = ?",
                    cleanupCartId
            );

            if (jdbcTemplate.update(
                    "DELETE FROM carts WHERE id = ?",
                    cleanupCartId
            ) != 1) {
                throw new IllegalStateException("Test cart cleanup failed");
            }
        });

        Integer availableAfterCleanup = jdbcTemplate.queryForObject(
                "SELECT quantity_available FROM product_stocks WHERE product_country_id = ?",
                Integer.class,
                cleanupProductCountryId
        );

        Integer reservedAfterCleanup = jdbcTemplate.queryForObject(
                "SELECT quantity_reserved FROM product_stocks WHERE product_country_id = ?",
                Integer.class,
                cleanupProductCountryId
        );

        assertEquals(cleanupAvailableBefore, availableAfterCleanup);
        assertEquals(cleanupReservedBefore, reservedAfterCleanup);

        System.out.println("=== CHECKOUT TEST CLEANUP ===");
        System.out.println("Stock restored: true");
        System.out.println("Test order and cart removed: true");
    }
    private boolean attemptCheckout(
            Long cartId,
            String contactEmail,
            CountDownLatch ready,
            CountDownLatch start
    ) throws Exception {
        Cart cart = cartRepository.findById(cartId).orElseThrow();

        AddressSnapshot address = new AddressSnapshot(
                "Demo Customer",
                "+48123456789",
                "PL",
                "Poland",
                null,
                "Warsaw",
                "00-001",
                "Test Street 1",
                null
        );

        ready.countDown();

        if (!start.await(15, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Start barrier timeout");
        }

        try {
            checkoutService.checkout(
                    cart,
                    null,
                    contactEmail,
                    address,
                    address,
                    null,
                    null,
                    null,
                    Carrier.DPD
            );

            return true;
        } catch (IllegalStateException exception) {
            if ("Cart already checked out".equals(exception.getMessage())) {
                return false;
            }

            throw exception;
        }
    }
}
