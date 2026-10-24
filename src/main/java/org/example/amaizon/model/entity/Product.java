package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.ProductValidationException;
import org.example.amaizon.model.embeddable.all.Brand;
import org.example.amaizon.model.embeddable.all.Sku;
import org.example.amaizon.model.embeddable.all.ImageUrl;
import org.example.amaizon.model.enums.ProductCountryStatus;
import org.example.amaizon.model.enums.ProductStatus;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.ProductValidation.*;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_products_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_products_global_code", columnNames = "global_code")
        }
)
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "stable_id", columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "brand", nullable = false))
    private Brand brand;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "global_code", nullable = false))
    private Sku sku;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "image_url", nullable = false))
    private ImageUrl imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final Set<ProductCountry> productCountries = new HashSet<>();

    @OneToMany(mappedBy = "product", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final Set<ProductAttributeValue> productAttributeValues = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, updatable = true)
    private Category category;

    protected Product() {}

    private Product(String brand, String sku, String imageUrl) {
        this.brand = mapOfBrand(brand);
        this.sku = mapOfGlobalCode(sku);
        this.imageUrl = mapOfImageUrl(imageUrl);
        this.status = ProductStatus.DRAFT;
    }

    public static Product createProduct(String brand, String globalCode, String imageUrl) {
        return new Product(brand, globalCode, imageUrl);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
        if (status == null) status = ProductStatus.DRAFT;
    }

    // ============================================================
    // == Domain Methods
    // ============================================================

    public void defineCategory(Category category) {
        if (this.category != null) ex(KEY_CATEGORY_ALREADY_EXISTS);
        setCategory(validateCategory(category));
    }

    public void updateCategory(Category category) {
        if (this.category == null) ex(KEY_CATEGORY_NOT_DEFINED);
        setCategory(validateCategory(category));
    }


    public void rebrand(String brand) {
        this.brand = mapOfBrand(brand);
    }

    public void changeImageUrl(String imageUrl) {
        this.imageUrl = mapOfImageUrl(imageUrl);
    }

    public void changeGlobalCode(String globalCode) {
        this.sku = mapOfGlobalCode(globalCode);
    }

    public void changeStatus(ProductStatus newStatus) {
        ProductStatus validated = validateProductStatus(newStatus);

        if (validated == ProductStatus.ACTIVE && this.category == null) {
            ex(KEY_ACTIVATE_WITHOUT_CATEGORY);
        }

        if (this.status == validated) return;
        this.status = validated;

        if (validated == ProductStatus.ACTIVE) {
            for (ProductCountry pc : productCountries) {
                pc.tryAutoActivate();
            }
        } else {
            for (ProductCountry pc : productCountries) {
                if (pc.getStatus() != ProductCountryStatus.DISABLED) {
                    pc.deactivate();
                }
            }
        }
    }

    public ProductCountry assignCountry(Country country) {
        Country validated = validateCountry(country);

        boolean exists = productCountries.stream()
                .anyMatch(pc -> pc.getCountry().equals(validated));
        if (exists) ex(KEY_COUNTRY_ALREADY_ASSIGNED);

        ProductCountry pc = ProductCountry.create(this, validated);
        pc.tryAutoActivate();
        return pc;
    }

    public void unassignCountry(Country country) {
        Country validated = validateCountry(country);

        ProductCountry existing = productCountries.stream()
                .filter(pc -> pc.getCountry().equals(validated))
                .findFirst()
                .orElse(null);

        if (existing == null) {
            ex(KEY_COUNTRY_NOT_ASSIGNED);
        }

        productCountries.remove(existing);
        validated._removeProductCountry(existing);
    }

    // ============================================================
    // == Relationship Management
    // ============================================================

    private void setCategory(Category newCategory) {
        if (Objects.equals(this.category, newCategory)) return;

        Category oldCategory = this.category;
        this.category = newCategory;

        if (oldCategory != null) oldCategory._removeProduct(this);
        if (newCategory != null) newCategory._addProduct(this);
    }

    void _addProductCountry(ProductCountry pc) {
        if (pc == null) return;
        this.productCountries.add(pc);
    }

    void _removeProductCountry(ProductCountry pc) {
        if (pc == null) return;
        this.productCountries.remove(pc);
    }

    public boolean isSellable() {
        return this.status == ProductStatus.ACTIVE;
    }

    // ============================================================
    // == Getters
    // ============================================================

    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public Brand getBrand() { return brand; }
    public Sku getSku() { return sku; }
    public ImageUrl getImageUrl() { return imageUrl; }
    public ProductStatus getStatus() { return status; }
    public Category getCategory() { return category; }
    public Set<ProductCountry> getProductCountries() { return Set.copyOf(productCountries); }
    public Set<ProductAttributeValue> getProductAttributeValues() { return Set.copyOf(productAttributeValues); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Product other = (Product) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // == Rules / Mapping
    // ============================================================

    private static Brand mapOfBrand(String raw) {
        return Brand.of(raw, BRAND_SPEC, BRAND_KEYS, Product::ex);
    }

    private static Sku mapOfGlobalCode(String raw) {
        return Sku.of(raw, SKU_SPEC, SKU_KEYS, Product::ex);
    }

    private static ImageUrl mapOfImageUrl(String raw) {
        return ImageUrl.of(raw, IMAGE_URL_SPEC, IMAGE_URL_KEYS, Product::ex);
    }

    private static ProductStatus validateProductStatus(ProductStatus status) {
        if (status == null) ex(KEY_STATUS_IS_NULL);
        return status;
    }

    private static Category validateCategory(Category category) {
        if (category == null) ex(KEY_CATEGORY_IS_NULL);
        return category;
    }

    private static Country validateCountry(Country country) {
        if (country == null) ex(KEY_COUNTRY_IS_NULL);
        return country;
    }

    private static void ex(String key, Object... values) {
        throw new ProductValidationException(key, values);
    }
}
