package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.CategoryTranslationValidationException;
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

import static org.example.amaizon.model.validators.validation.CategoryTranslationValidation.*;

@Entity
@Table(
        name = "category_translations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_translations_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_category_translations_lang", columnNames = {"category_country_id", "language_code"}),
                @UniqueConstraint(name = "uk_category_translations_category_country_id", columnNames = "category_country_id")
        },
        indexes = {
                @Index(name = "idx_category_translations_country", columnList = "category_country_id"),
                @Index(name = "idx_category_translations_language", columnList = "language_code")
        }
)
public class CategoryTranslation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", name = "stable_id", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "language_code", nullable = false, updatable = false))
    private LanguageCode languageCode;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false))
    private DisplayName displayName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "description", nullable = false, columnDefinition = "TEXT"))
    private Description description;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "slug", nullable = false))
    private Slug slug;


    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_country_id", nullable = false, updatable = false)
    private CategoryCountry categoryCountry;

    protected CategoryTranslation() {}

    private CategoryTranslation(
            String languageCode,
            String displayName,
            String description,
            String slug,
            CategoryCountry categoryCountry
    ) {
        this.languageCode = mapOfLanguageCode(languageCode);
        this.displayName = mapOfName(displayName);
        this.description = mapOfDescription(description);

        if (slug == null || slug.isBlank()) {

            regenerateSlug();
        } else {
            this.slug = mapOfSlug(slug);
        }

        _attachToCategoryCountry(validateCategoryCountry(categoryCountry));
    }

    public static CategoryTranslation create(
            String languageCode,
            String displayName,
            String description,
            String slug,
            CategoryCountry categoryCountry
    ) {
        return new CategoryTranslation(languageCode, displayName, description, slug, categoryCountry);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    // ============================================================
    // == Domain
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
    }

    public void changeDisplayName(String displayName) {
        this.displayName = mapOfName(displayName);
    }

    public void changeDescription(String description) {
        this.description = mapOfDescription(description);
    }

    public void changeSlug(String slug) {
        this.slug = mapOfSlug(slug);
    }

    public void changeAll(String displayName, String description, String slug) {
        changeDisplayName(displayName);
        changeDescription(description);

        if (slug == null || slug.isBlank()) {
            regenerateSlug();
        } else {
            changeSlug(slug);
        }
    }

    // ============================================================
    // == Relationship Management: CategoryCountry (OWNER SIDE)
    // ============================================================

    void _attachToCategoryCountry(CategoryCountry newOwner) {
        if (newOwner == null) {
            ex(KEY_CATEGORY_COUNTRIES_IS_NULL);
        }
        if (this.categoryCountry == newOwner) return;

        CategoryCountry old = this.categoryCountry;
        this.categoryCountry = newOwner;

        if (old != null) {
            old._clearTranslationDirect(this);
        }
        newOwner._setTranslationDirect(this);
    }

    void _detachFromCategoryCountry() {
        CategoryCountry old = this.categoryCountry;
        if (old == null) return;

        this.categoryCountry = null;
        old._clearTranslationDirect(this);
    }

    // ============================================================
    // == Getters
    // ============================================================

    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public LanguageCode getLanguageCode() { return languageCode; }
    public DisplayName getDisplayName() { return displayName; }
    public Description getDescription() { return description; }
    public Slug getSlug() { return slug; }
    public CategoryCountry getCategoryCountry() { return categoryCountry; }

    // ============================================================
    // == Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CategoryTranslation other = (CategoryTranslation) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // == Rules
    // ============================================================

    private static CategoryCountry validateCategoryCountry(CategoryCountry categoryCountry) {
        if (categoryCountry == null) ex(KEY_CATEGORY_COUNTRIES_IS_NULL);
        return categoryCountry;
    }

    private static DisplayName mapOfName(String displayName) {
        return DisplayName.of(displayName, DISPLAY_NAME_SPEC, DISPLAY_NAME_KEYS, CategoryTranslation::ex);
    }

    private static Description mapOfDescription(String description) {
        return Description.of(description, DESCRIPTION_SPEC, DESCRIPTION_KEYS, CategoryTranslation::ex);
    }

    private static LanguageCode mapOfLanguageCode(String languageCode) {
        return LanguageCode.of(languageCode, LANG_CODE_SPEC, LANG_CODE_KEYS, CategoryTranslation::ex);
    }

    private static Slug mapOfSlug(String slug) {
        return Slug.of(slug, SLUG_SPEC, SLUG_KEYS, CategoryTranslation::ex);
    }

    private static void ex(String key, Object... args) {
        throw new CategoryTranslationValidationException(key, args);
    }
}
