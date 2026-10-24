package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.ProductPriceValidationException;
import org.example.amaizon.model.embeddable.all.Price;
import org.example.amaizon.model.enums.ProductPriceStatus;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.ProductPriceValidation.*;

@Entity
@Table(
        name = "product_prices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_prices_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_product_prices_product_country_id", columnNames = "product_country_id")
        },
        indexes = {
                @Index(name = "ix_product_prices_product_country_id", columnList = "product_country_id"),
                @Index(name = "ix_product_prices_status", columnList = "status"),
                @Index(name = "ix_product_prices_valid_to", columnList = "valid_to")
        }
)
public class ProductPrice extends AuditableEntity {

    private static void ex(String key, Object... args) {
        throw new ProductPriceValidationException(key, args);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "stable_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID stableId = UUID.randomUUID();

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "price", nullable = false, precision = 10, scale = 2))
    private Price price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductPriceStatus status;

    @Column(name = "valid_to")
    private Instant validTo;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_country_id", nullable = false, updatable = false, unique = true)
    private ProductCountry productCountry;

    protected ProductPrice() {
    }

    private ProductPrice(BigDecimal price, ProductCountry productCountry, ProductPriceStatus status, Instant validTo) {
        this.price = mapOfPrice(price);
        this.status = Objects.requireNonNullElse(status, ProductPriceStatus.DRAFT);
        this.validTo = validateValidTo(validTo);
        _attachToProductCountry(validateProductCountry(productCountry));
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
        if (status == null) status = ProductPriceStatus.DRAFT;
    }

    public static ProductPrice createDraft(BigDecimal price, ProductCountry productCountry, Instant validTo) {
        return new ProductPrice(price, productCountry, ProductPriceStatus.DRAFT, validTo);
    }

    public static ProductPrice createCurrent(BigDecimal price, ProductCountry productCountry, Instant validTo) {
        return new ProductPrice(price, productCountry, ProductPriceStatus.CURRENT, validTo);
    }

    // ============================================================
    // Domain
    // ============================================================

    public void changePrice(BigDecimal newPrice) {
        this.price = mapOfPrice(newPrice);
        notifyProductCountry();
    }

    public void changeStatus(ProductPriceStatus newStatus) {
        newStatus = validateStatus(newStatus);
        if (this.status == newStatus) return;
        this.status = newStatus;
        notifyProductCountry();
    }

    public void changeValidTo(Instant newValidTo) {
        this.validTo = validateValidTo(newValidTo);
        notifyProductCountry();
    }

    public void deactivate() {
        if (this.status == ProductPriceStatus.INACTIVE) return;
        this.status = ProductPriceStatus.INACTIVE;
        notifyProductCountry();
    }

    public boolean isCurrentlyValid() {
        return validTo == null || validTo.isAfter(Instant.now());
    }

    public boolean isCurrent() {
        return status == ProductPriceStatus.CURRENT && isCurrentlyValid();
    }

    // ============================================================
    // Relationship management (OWNER)
    // ============================================================

    void _attachToProductCountry(ProductCountry pc) {
        if (pc == null) ex(KEY_PRODUCT_COUNTRY_NULL);
        if (this.productCountry == pc) return;

        ProductCountry old = this.productCountry;
        this.productCountry = pc;

        if (old != null) old._removePriceInternal(this);
        pc._addPriceInternal(this);

        notifyProductCountry();
    }

    void _detachFromProductCountry() {
        ProductCountry old = this.productCountry;
        if (old == null) return;

        this.productCountry = null;
        old._removePriceInternal(this);


        old.onPriceChanged();
    }

    private void notifyProductCountry() {
        if (this.productCountry != null) {
            this.productCountry.onPriceChanged();
        }
    }

    // ============================================================
    // Getters
    // ============================================================

    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public Price getPrice() { return price; }
    public java.math.BigDecimal getPriceValue() {
        return price != null ? price.getValue() : null;
    }
    public ProductPriceStatus getStatus() { return status; }
    public Instant getValidTo() { return validTo; }
    public ProductCountry getProductCountry() { return productCountry; }


    // ============================================================
    // Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProductPrice other = (ProductPrice) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // Validation / Mapping
    // ============================================================

    private static ProductPriceStatus validateStatus(ProductPriceStatus status) {
        if (status == null) ex(KEY_STATUS_NULL);
        return status;
    }

    private static ProductCountry validateProductCountry(ProductCountry productCountry) {
        if (productCountry == null) ex(KEY_PRODUCT_COUNTRY_NULL);
        return productCountry;
    }

    private static Instant validateValidTo(Instant validTo) {
        if (validTo == null) return null;
        if (validTo.isBefore(Instant.now())) ex(KEY_VALID_TO_IN_PAST);
        return validTo;
    }

    private static Price mapOfPrice(BigDecimal price) {
        return Price.of(price, PRICE_KEYS, ProductPrice::ex);
    }
}
