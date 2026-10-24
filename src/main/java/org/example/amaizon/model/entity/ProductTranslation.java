package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.ProductTranslationValidationException;
import org.example.amaizon.model.embeddable.all.Description;
import org.example.amaizon.model.embeddable.all.DisplayName;
import org.example.amaizon.model.embeddable.all.LanguageCode;
import org.example.amaizon.model.embeddable.all.Slug;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.ProductTranslationValidation.*;

@Entity
@Table(
        name = "product_translations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_translations_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name="uk_product_translations_product_country_id", columnNames="product_country_id")
        },
        indexes = {
                @Index(name = "ix_product_translations_product_country_id", columnList = "product_country_id"),
                @Index(name = "ix_product_translations_language_code", columnList = "language_code")
        }
)
public class ProductTranslation extends AuditableEntity {

    private static void ex(String key, Object... values) {
        throw new ProductTranslationValidationException(key, values);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "stable_id", columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false))
    private DisplayName displayName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "description", nullable = false, columnDefinition = "TEXT"))
    private Description description;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "slug", nullable = false))
    private Slug slug;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "language_code", nullable = false, updatable = false))
    private LanguageCode languageCode;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_country_id", nullable = false, updatable = false, unique = true)
    private ProductCountry productCountry;

    protected ProductTranslation() {}

    private ProductTranslation(String displayName, String languageCode, String slug, String description, ProductCountry productCountry) {
        this.displayName = mapOfDisplayName(displayName);
        this.languageCode = mapOfLanguageCode(languageCode);
        this.description = mapOfDescription(description);

        if (slug == null || slug.isBlank()) regenerateSlug();
        else this.slug = mapOfSlug(slug);

        _attachToProductCountry(productCountry);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static ProductTranslation create(String displayName, String languageCode, String slug, String description, ProductCountry productCountry) {
        return new ProductTranslation(displayName, languageCode, slug, description, productCountry);
    }

    // ============================================================
    // Relationship management (OWNER)
    // ============================================================

    void _attachToProductCountry(ProductCountry pc) {
        if (pc == null) ex(KEY_PRODUCT_COUNTRY_IS_NULL);
        if (this.productCountry == pc) return;

        ProductCountry old = this.productCountry;
        this.productCountry = pc;

        if (old != null) old._removeTranslationDirect(this);
        pc._addTranslationDirect(this);

        pc.onTranslationChanged();
    }

    void _detachFromProductCountry() {
        ProductCountry old = this.productCountry;
        if (old == null) return;

        this.productCountry = null;
        old._removeTranslationDirect(this);


        old.onTranslationChanged();
    }

    // ============================================================
    // Domain
    // ============================================================

    public void regenerateSlug() {
        String base = displayName.getValue().trim().toLowerCase(Locale.ROOT);
        String normalized = java.text.Normalizer.normalize(base, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        String generated = normalized
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        this.slug = mapOfSlug(generated);
        if (this.productCountry != null) this.productCountry.onTranslationChanged();
    }

    public void changeDisplayName(String displayName) {
        this.displayName = mapOfDisplayName(displayName);
        if (this.productCountry != null) this.productCountry.onTranslationChanged();
    }

    public void changeSlug(String newSlug) {
        this.slug = mapOfSlug(newSlug);
        if (this.productCountry != null) this.productCountry.onTranslationChanged();
    }

    public void changeDescription(String newDescription) {
        this.description = mapOfDescription(newDescription);
        if (this.productCountry != null) this.productCountry.onTranslationChanged();
    }

    public void changeAll(String displayName, String description, String slug) {
        changeDisplayName(displayName);
        changeDescription(description);
        if (slug == null || slug.isBlank()) regenerateSlug();
        else changeSlug(slug);
    }

    // ============================================================
    // Getters
    // ============================================================

    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public DisplayName getDisplayName() { return displayName; }
    public Description getDescription() { return description; }
    public Slug getSlug() { return slug; }
    public ProductCountry getProductCountry() { return productCountry; }
    public LanguageCode getLanguageCode() { return languageCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProductTranslation other = (ProductTranslation) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() { return Objects.hash(stableId); }

    private static DisplayName mapOfDisplayName(String displayName) {
        return DisplayName.of(displayName, DISPLAY_NAME_SPEC, DISPLAY_NAME_KEYS, ProductTranslation::ex);
    }

    private static Description mapOfDescription(String description) {
        return Description.of(description, DESCRIPTION_SPEC, DESCRIPTION_KEYS, ProductTranslation::ex);
    }

    private static Slug mapOfSlug(String slug) {
        return Slug.of(slug, SLUG_SPEC, SLUG_KEYS, ProductTranslation::ex);
    }

    private static LanguageCode mapOfLanguageCode(String languageCode) {
        return LanguageCode.of(languageCode, LANG_CODE_SPEC, LANG_CODE_KEYS, ProductTranslation::ex);
    }
}
