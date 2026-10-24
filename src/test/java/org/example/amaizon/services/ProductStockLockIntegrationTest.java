package org.example.amaizon.services;

import org.example.amaizon.model.entity.ProductCountry;
import org.example.amaizon.model.entity.ProductStock;
import org.example.amaizon.repositories.ProductCountryRepository;
import org.example.amaizon.repositories.ProductStockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductStockLockIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate transactions;

    @Autowired
    private ProductStockRepository stocks;

    @Autowired
    private ProductCountryRepository countries;

    private Long stockId;
    private Integer availableBefore;
    private Integer reservedBefore;

    @Test
    void adminIncreaseAndCheckoutReservationDoNotLoseStockUpdates()
            throws Exception {

        stockId = jdbc.queryForObject(
                """
                SELECT id
                FROM product_stocks
                WHERE quantity_available >= 2
                ORDER BY id
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

        Long productId = jdbc.queryForObject(
                "SELECT product_id FROM product_countries WHERE id = ?",
                Long.class,
                productCountryId
        );

        Long countryId = jdbc.queryForObject(
                "SELECT country_id FROM product_countries WHERE id = ?",
                Long.class,
                productCountryId
        );

        assertNotNull(productId);
        assertNotNull(countryId);

        availableBefore = available();
        reservedBefore = reserved();

        CountDownLatch adminLocked = new CountDownLatch(1);
        CountDownLatch releaseAdmin = new CountDownLatch(1);
        CountDownLatch checkoutStarted = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> admin = executor.submit(() ->
                    transactions.executeWithoutResult(status -> {
                        ProductCountry pc = countries.findById(
                                productCountryId
                        ).orElseThrow();

                        ProductStock stock = stocks
                                .findByProductCountryForUpdate(pc)
                                .orElseThrow();

                        stock.increaseAvailable(4);

                        adminLocked.countDown();

                        try {
                            if (!releaseAdmin.await(15, TimeUnit.SECONDS)) {
                                throw new IllegalStateException(
                                        "Admin lock timeout"
                                );
                            }
                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException(exception);
                        }
                    })
            );

            assertTrue(adminLocked.await(15, TimeUnit.SECONDS));

            Future<?> checkout = executor.submit(() -> {
                checkoutStarted.countDown();

                transactions.executeWithoutResult(status -> {
                    ProductStock stock = stocks
                            .findByProductIdAndCountryIdForUpdate(
                                    productId,
                                    countryId
                            ).orElseThrow();

                    stock.reserve(1);
                });
            });

            assertTrue(checkoutStarted.await(15, TimeUnit.SECONDS));

            try {
                Thread.sleep(250);
                assertFalse(
                        checkout.isDone(),
                        "Checkout should wait for the stock lock"
                );
            } finally {
                releaseAdmin.countDown();
            }

            admin.get(30, TimeUnit.SECONDS);
            checkout.get(30, TimeUnit.SECONDS);

            assertEquals(availableBefore + 3, available());
            assertEquals(reservedBefore + 1, reserved());
        } finally {
            releaseAdmin.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
        }
    }

    @AfterEach
    void restoreStock() {
        if (stockId == null || availableBefore == null
                || reservedBefore == null) {
            return;
        }

        transactions.executeWithoutResult(status -> {
            Integer currentAvailable = jdbc.queryForObject(
                    """
                    SELECT quantity_available
                    FROM product_stocks
                    WHERE id = ?
                    FOR UPDATE
                    """,
                    Integer.class,
                    stockId
            );

            Integer currentReserved = jdbc.queryForObject(
                    """
                    SELECT quantity_reserved
                    FROM product_stocks
                    WHERE id = ?
                    """,
                    Integer.class,
                    stockId
            );

            if (currentAvailable == null || currentReserved == null) {
                throw new IllegalStateException("Stock missing during cleanup");
            }

            boolean unchanged =
                    currentAvailable.equals(availableBefore)
                            && currentReserved.equals(reservedBefore);

            boolean adminOnly =
                    currentAvailable.equals(availableBefore + 4)
                            && currentReserved.equals(reservedBefore);

            boolean checkoutOnly =
                    currentAvailable.equals(availableBefore - 1)
                            && currentReserved.equals(reservedBefore + 1);

            boolean completed =
                    currentAvailable.equals(availableBefore + 3)
                            && currentReserved.equals(reservedBefore + 1);

            if (!unchanged && !adminOnly && !checkoutOnly && !completed) {
                throw new IllegalStateException(
                        "Unexpected stock state; refusing cleanup"
                );
            }

            if (!unchanged) {
                int updated = jdbc.update(
                        """
                        UPDATE product_stocks
                        SET quantity_available = ?,
                            quantity_reserved = ?
                        WHERE id = ?
                        """,
                        availableBefore,
                        reservedBefore,
                        stockId
                );

                if (updated != 1) {
                    throw new IllegalStateException("Stock cleanup failed");
                }
            }
        });

        assertEquals(availableBefore, available());
        assertEquals(reservedBefore, reserved());
    }

    private int available() {
        return jdbc.queryForObject(
                "SELECT quantity_available FROM product_stocks WHERE id = ?",
                Integer.class,
                stockId
        );
    }

    private int reserved() {
        return jdbc.queryForObject(
                "SELECT quantity_reserved FROM product_stocks WHERE id = ?",
                Integer.class,
                stockId
        );
    }
}
