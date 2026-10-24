package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.ProductCountryValidationException;
import org.example.amaizon.model.enums.CountryStatus;
import org.example.amaizon.model.enums.ProductCountryStatus;
import org.example.amaizon.model.enums.ProductPriceStatus;
import org.example.amaizon.model.enums.ProductStatus;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.ProductCountryValidation.*;

@Entity
@Table(
        name = "product_countries",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_country_ids", columnNames = {"product_id", "country_id"}),
                @UniqueConstraint(name = "uk_product_country_stable_id", columnNames = "stable_id")
        }
)
public class ProductCountry extends AuditableEntity {

    private static void ex(String key, Object... values) {
        throw new ProductCountryValidationException(key, values);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "stable_id", columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCountryStatus status;


    @OneToOne(mappedBy = "productCountry", orphanRemoval = true, cascade = CascadeType.ALL)
    private ProductPrice price;

    @OneToOne(mappedBy = "productCountry", orphanRemoval = true, cascade = CascadeType.ALL)
    private ProductTranslation translation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false, updatable = false)
    private Country country;

    protected ProductCountry() {
    }

    private ProductCountry(Product product, Country country) {
        _attachProduct(validateProduct(product));
        _attachCountry(validateCountry(country));
        this.status = ProductCountryStatus.DRAFT;
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
        if (status == null) status = ProductCountryStatus.DRAFT;
    }

    public static ProductCountry create(Product product, Country country) {
        return new ProductCountry(product, country);
    }


    public void deactivate() {
        if (this.status == ProductCountryStatus.INACTIVE) return;
        this.status = ProductCountryStatus.INACTIVE;
    }

    public void activateOrThrow() {
        if (this.status == ProductCountryStatus.ACTIVE) return;

        if (this.country.getStatus() != CountryStatus.ACTIVE) ex(KEY_ACTIVATE_WITH_INACTIVE_COUNTRY);
        if (this.translation == null) ex(KEY_ACTIVATE_WITHOUT_TRANSLATION);
        if (this.price == null) ex(KEY_ACTIVATE_WITHOUT_PRICE);
        if (!this.price.isCurrent()) ex(KEY_ACTIVATE_WITHOUT_CURRENT_PRICE);

        activate();
    }

    public void tryAutoActivate() {
        if (this.status == ProductCountryStatus.ACTIVE) return;
        if (this.status == ProductCountryStatus.DISABLED) return;
        if (this.country.getStatus() != CountryStatus.ACTIVE) return;
        if (this.product.getStatus() != ProductStatus.ACTIVE) return;
        if (this.translation == null) return;
        if (this.price == null) return;
        if (!this.price.isCurrent()) return;

        activate();
    }

    private void activate() {
        this.status = ProductCountryStatus.ACTIVE;
    }

    // ============================================================
    // == Translation
    // ============================================================

    public ProductTranslation createTranslation(String name, String languageCode, String slug, String description) {
        if (this.translation != null) ex(KEY_TRANSLATION_ALREADY_DEFINED);

        ProductTranslation created = ProductTranslation.create(name, languageCode, slug, description, this);


        tryAutoActivate();
        return created;
    }

    public ProductTranslation updateTranslation(String name, String slug, String description) {
        if (this.translation == null) ex(KEY_TRANSLATION_NOT_DEFINED);

        this.translation.changeAll(name, description, slug);

        tryAutoActivate();
        return this.translation;
    }

    public void removeTranslation() {
        if (this.translation == null) return;
        deactivate();

        ProductTranslation old = this.translation;
        old._detachFromProductCountry();
    }

    void _addTranslationDirect(ProductTranslation translation) {
        if (translation == null) return;
        this.translation = translation;
    }

    void _removeTranslationDirect(ProductTranslation translation) {
        if (translation == null) return;
        if (this.translation != translation) return;
        this.translation = null;
    }

    // ============================================================
    // == Price
    // ============================================================

    public ProductPrice createPrice(BigDecimal price, ProductPriceStatus status, Instant validTo) {
        if (this.price == null) {
            ProductPrice created = (status == ProductPriceStatus.CURRENT)
                    ? ProductPrice.createCurrent(price, this, validTo)
                    : ProductPrice.createDraft(price, this, validTo);
            tryAutoActivate();
            return created;
        }
        return updatePrice(price, status, validTo);
    }

    public ProductPrice updatePrice(BigDecimal price, ProductPriceStatus status, Instant validTo) {
        if (this.price == null) ex(KEY_PRICE_NOT_DEFINED);

        if (price != null) this.price.changePrice(price);
        this.price.changeValidTo(validTo);

        if (status != null) this.price.changeStatus(status);

        return this.price;
    }

    public void removePrice() {
        if (this.price == null) return;
        if (this.status == ProductCountryStatus.ACTIVE) deactivate();


        ProductPrice old = this.price;
        old._detachFromProductCountry();
    }


    public void onPriceChanged() {
        if (this.price == null || !this.price.isCurrent()) {
            if (this.status == ProductCountryStatus.ACTIVE) deactivate();
            return;
        }
        tryAutoActivate();
    }

    public void onTranslationChanged() {
        if (this.translation == null) {
            if (this.status == ProductCountryStatus.ACTIVE) deactivate();
            return;
        }
        tryAutoActivate();
    }


    private void _attachProduct(Product newProduct) {
        if (this.product == newProduct) return;
        Product old = this.product;
        this.product = newProduct;
        if (old != null) old._removeProductCountry(this);
        if (newProduct != null) newProduct._addProductCountry(this);
    }

    private void _attachCountry(Country newCountry) {
        if (this.country == newCountry) return;
        Country old = this.country;
        this.country = newCountry;
        if (old != null) old._removeProductCountry(this);
        if (newCountry != null) newCountry._addProductCountry(this);
    }

    void _addPriceInternal(ProductPrice price) {
        this.price = price;
    }

    void _removePriceInternal(ProductPrice price) {
        if (this.price == price) {
            this.price = null;
        }
    }


    public long getId() {
        return id;
    }

    public UUID getStableId() {
        return stableId;
    }

    public ProductCountryStatus getStatus() {
        return status;
    }

    public ProductPrice getPrice() {
        return price;
    }

    public ProductTranslation getTranslation() {
        return translation;
    }

    public Product getProduct() {
        return product;
    }

    public Country getCountry() {
        return country;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProductCountry other = (ProductCountry) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }


    private static Country validateCountry(Country country) {
        if (country == null) ex(KEY_COUNTRY_IS_NULL);
        return country;
    }

    private static Product validateProduct(Product product) {
        if (product == null) ex(KEY_PRODUCT_IS_NULL);
        return product;
    }
}
