package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.ProductStockValidationException;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import static org.example.amaizon.model.validators.validation.ProductStockValidation.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "product_stocks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_stocks_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_product_stocks_product_country", columnNames = {"product_country_id"})
        },
        indexes = {
                @Index(name = "idx_product_stocks_product_country", columnList = "product_country_id")
        }
)
public class ProductStock extends AuditableEntity {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "stable_id", nullable = false, updatable = false)
    private UUID stableId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_country_id", nullable = false)
    private ProductCountry productCountry;

    @Column(name = "quantity_available", nullable = false)
    private int quantityAvailable;

    @Column(name = "quantity_reserved", nullable = false)
    private int quantityReserved;

    protected ProductStock() {
    }

    private ProductStock(ProductCountry productCountry, int quantityAvailable) {
        this.stableId = UUID.randomUUID();
        this.productCountry = validateProductCountry(productCountry);
        this.quantityAvailable = validateNonNegative(quantityAvailable);
        this.quantityReserved = 0;
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
        if (quantityAvailable < 0) ex(KEY_QTY_NEGATIVE);
        if (quantityReserved < 0) ex(KEY_QTY_NEGATIVE);
    }

    public static ProductStock create(ProductCountry productCountry, int initialAvailable) {
        return new ProductStock(productCountry, initialAvailable);
    }

    // ============================================================
    // == Domain methods
    // ============================================================

    public void increaseAvailable(int by) {
        int inc = validateNonNegative(by);
        this.quantityAvailable += inc;
    }

    public void decreaseAvailable(int by) {
        int dec = validateNonNegative(by);
        if (this.quantityAvailable < dec) ex(KEY_NOT_ENOUGH_AVAILABLE, this.quantityAvailable, dec);
        this.quantityAvailable -= dec;
    }


    public void reserve(int qty) {
        int q = validateNonNegative(qty);
        if (this.quantityAvailable < q) ex(KEY_NOT_ENOUGH_AVAILABLE, this.quantityAvailable, q);

        this.quantityAvailable -= q;
        this.quantityReserved += q;
    }


    public void release(int qty) {
        int q = validateNonNegative(qty);
        if (this.quantityReserved < q) ex(KEY_NOT_ENOUGH_RESERVED, this.quantityReserved, q);

        this.quantityReserved -= q;
        this.quantityAvailable += q;
    }


    public void commitReserved(int qty) {
        int q = validateNonNegative(qty);
        if (this.quantityReserved < q) ex(KEY_NOT_ENOUGH_RESERVED, this.quantityReserved, q);
        this.quantityReserved -= q;
    }

    // ============================================================
    // == Getters
    // ============================================================

    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public ProductCountry getProductCountry() { return productCountry; }
    public int getQuantityAvailable() { return quantityAvailable; }
    public int getQuantityReserved() { return quantityReserved; }

    // ============================================================
    // == Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProductStock other = (ProductStock) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // == Validation
    // ============================================================

    private static ProductCountry validateProductCountry(ProductCountry pc) {
        if (pc == null) ex(KEY_PRODUCT_COUNTRY_IS_NULL);
        return pc;
    }

    private static int validateNonNegative(int qty) {
        if (qty < 0) ex(KEY_QTY_NEGATIVE, qty);
        return qty;
    }

    private static void ex(String key, Object... args) {
        throw new ProductStockValidationException(key, args);
    }
}
