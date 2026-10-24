package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.CategoryCountryValidationException;
import org.example.amaizon.model.enums.CategoryCountryStatus;
import org.example.amaizon.model.enums.CategoryStatus;
import org.example.amaizon.model.enums.CountryStatus;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.CategoryCountryValidation.*;

@Entity
@Table(
        name = "category_countries",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_countries_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_category_countries_ids", columnNames = {"country_id", "category_id"})
        }
)
public class CategoryCountry extends AuditableEntity {

    private static void ex(String key, Object... values) {
        throw new CategoryCountryValidationException(key, values);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", name = "stable_id", updatable = false, nullable = false)
    private UUID stableId = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryCountryStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, updatable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false, updatable = false)
    private Country country;


    @OneToOne(mappedBy = "categoryCountry", orphanRemoval = true, cascade = CascadeType.ALL)
    private CategoryTranslation translation;

    protected CategoryCountry() {}

    private CategoryCountry(Category category, Country country) {
        _attachCategory(validateCategory(category));
        _attachCountry(validateCountry(country));
        this.status = CategoryCountryStatus.DRAFT;
    }

    public static CategoryCountry create(Category category, Country country) {
        return new CategoryCountry(category, country);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
        if (status == null) status = CategoryCountryStatus.DRAFT;
    }



// ============================================================
// == Relationship Management: Translation
// ============================================================

    public CategoryTranslation createTranslation(String languageCode, String name, String description, String slug) {
        if (this.translation != null) {
            ex(KEY_TRANSLATION_ALREADY_EXISTS);
        }

        CategoryTranslation created = CategoryTranslation.create(languageCode, name, description, slug, this);
        tryToActivate();
        return created;
    }

    public CategoryTranslation updateTranslation(String name, String description, String slug) {
        if (this.translation == null) {
            ex(KEY_TRANSLATION_NOT_DEFINED);
        }

        this.translation.changeAll(name, description, slug);
        tryToActivate();
        return this.translation;
    }

    public void removeTranslation() {
        if (this.translation == null) return;

        deactivate();

        this.translation._detachFromCategoryCountry();

    }


    void _setTranslationDirect(CategoryTranslation t) {
        this.translation = t;
    }


    void _clearTranslationDirect(CategoryTranslation t) {
        if (this.translation == t) {
            this.translation = null;
        }
    }


    // ============================================================
    // == Activation
    // ============================================================

    private void tryToActivate() {
        if (this.status == CategoryCountryStatus.ACTIVE) return;
        if (this.status == CategoryCountryStatus.DISABLED) return;

        if (this.category.getStatus() != CategoryStatus.ACTIVE) return;
        if (this.country.getStatus() != CountryStatus.ACTIVE) return;
        if (this.translation == null) return;

        activate();
    }

    public void activateOrThrow() {
        if (this.status == CategoryCountryStatus.ACTIVE) return;

        if (this.country.getStatus() != CountryStatus.ACTIVE) {
            ex(KEY_ACTIVATE_WITH_INACTIVE_COUNTRY);
        }
        if (this.category.getStatus() != CategoryStatus.ACTIVE) {
            ex(KEY_ACTIVATE_WITH_INACTIVE_CATEGORY);
        }
        if (this.translation == null) {
            ex(KEY_ACTIVATE_WITHOUT_TRANSLATION);
        }

        activate();
    }

    public void activate() { this.status = CategoryCountryStatus.ACTIVE; }
    public void deactivate() { this.status = CategoryCountryStatus.INACTIVE; }
    public void disable() { this.status = CategoryCountryStatus.DISABLED; }

    // ============================================================
    // == Relationship Management
    // ============================================================

    private void _attachCategory(Category newCategory) {
        if (this.category == newCategory) return;

        Category old = this.category;
        this.category = newCategory;

        if (old != null) old._removeCategoryCountry(this);
        if (newCategory != null) newCategory._addCategoryCountry(this);
    }

    private void _attachCountry(Country newCountry) {
        if (this.country == newCountry) return;

        Country old = this.country;
        this.country = newCountry;

        if (old != null) old._removeCategoryCountry(this);
        if (newCountry != null) newCountry._addCategoryCountry(this);
    }

    // ============================================================
    // == Getters
    // ============================================================

    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public CategoryCountryStatus getStatus() { return status; }
    public Category getCategory() { return category; }
    public Country getCountry() { return country; }
    public CategoryTranslation getTranslation() { return translation; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(o) != Hibernate.getClass(this)) return false;
        CategoryCountry other = (CategoryCountry) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override public int hashCode() { return Objects.hash(stableId); }

    private static Category validateCategory(Category category) {
        if (category == null) ex(KEY_CATEGORY_IS_NULL);
        return category;
    }

    private static Country validateCountry(Country country) {
        if (country == null) ex(KEY_COUNTRY_IS_NULL);
        return country;
    }
}
