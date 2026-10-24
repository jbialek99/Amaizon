package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.AttributeOptionValidationException;
import org.example.amaizon.model.embeddable.all.Code;
import org.example.amaizon.model.embeddable.all.DisplayOrder;

import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.AttributeOptionValidation.*;

@Entity
@Table(
        name = "attribute_options",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attribute_options_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_attribute_options_code", columnNames = {"attribute_definition_id", "code"})
        },
        indexes = {
                @Index(name = "idx_attribute_options_definition", columnList = "attribute_definition_id"),
                @Index(name = "idx_attribute_options_active", columnList = "is_active"),
                @Index(name = "idx_attribute_options_def_order", columnList = "attribute_definition_id,display_order")
        }
)
public class AttributeOption extends AuditableEntity {

    private static void ex(String key, Object... args) {
        throw new AttributeOptionValidationException(key, args);
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", name = "stable_id", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "code", nullable = false)
    )
    private Code code;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "display_order", nullable = false)
    )
    private DisplayOrder displayOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_definition_id", nullable = false, updatable = false)
    private AttributeDefinition attributeDefinition;

    @OneToMany(
            mappedBy = "attributeOption",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private final Set<AttributeOptionTranslation> translations = new HashSet<>();

    // ============================================================
    // == Constructors / Factory
    // ============================================================

    protected AttributeOption() {
    }

    private AttributeOption(String code, boolean active, Integer displayOrder, AttributeDefinition definition) {
        this.code = mapOfCode(code);
        this.active = active;
        this.displayOrder = mapOfDisplayOrder(displayOrder);
        _attachAttributeDefinition(definition);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static AttributeOption create(String code, boolean active, Integer displayOrder, AttributeDefinition definition) {
        return new AttributeOption(code, active, displayOrder, definition);
    }

    // ============================================================
    // == Domain methods
    // ============================================================

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void changeCode(String code) {
        this.code = mapOfCode(code);
    }

    public void changeDisplayOrder(Integer displayOrder) {
        this.displayOrder = mapOfDisplayOrder(displayOrder);
    }

    public void changeAll(String code, Integer displayOrder, boolean active) {
        changeCode(code);
        changeDisplayOrder(displayOrder);
        this.active = active;

    }


    // ============================================================
    // == Relationship Management: AttributeDefinition
    // ============================================================

    void _attachAttributeDefinition(AttributeDefinition ad) {
        if (ad == null)
            ex(KEY_ATTRIBUTE_DEFINITION_NULL);
        if (this.attributeDefinition == ad) return;

        AttributeDefinition old = this.attributeDefinition;
        this.attributeDefinition = ad;

        if (old != null)
            old._removeAttributeOptionDirect(this);

        ad._addAttributeOptionDirect(this);
    }

    void _detachAttributeDefinition() {
        AttributeDefinition old = this.attributeDefinition;
        if (old == null) return;

        this.attributeDefinition = null;
        old._removeAttributeOptionDirect(this);
    }


    // ============================================================
    // == Relationship Management: Translations
    // ============================================================

    public AttributeOptionTranslation createTranslation(String languageCode, String name) {
        if (languageCode == null)
            ex(KEY_LANG_CODE_NULL);

        AttributeOptionTranslation existing = findTranslationByLanguage(languageCode);
        if (existing != null)
            ex(KEY_TRANSLATION_ALREADY_EXISTS, languageCode);


        return AttributeOptionTranslation.create(languageCode, name, this);
    }

    public AttributeOptionTranslation updateTranslation(String languageCode, String name) {
        if (languageCode == null)
            ex(KEY_LANG_CODE_NULL);

        AttributeOptionTranslation existing = findTranslationByLanguage(languageCode);
        if (existing == null)
            ex(KEY_TRANSLATION_NOT_FOUND, languageCode);

        existing.changeAll(name);
        return existing;
    }

    public void removeTranslationByLanguageCode(String languageCode) {
        if (languageCode == null)
            ex(KEY_LANG_CODE_NULL);

        AttributeOptionTranslation existing = findTranslationByLanguage(languageCode);
        if (existing == null) {
            ex(KEY_TRANSLATION_NOT_FOUND, languageCode);
        }
        existing._detachAttributeOption();
    }

    public void removeTranslation(AttributeOptionTranslation t) {
        if (t == null) return;
        t._detachAttributeOption();
    }

    public AttributeOptionTranslation findTranslationByLanguage(String languageCode) {
        if (languageCode == null)
            return null;

        for (AttributeOptionTranslation t : translations) {
            if (t.getLanguageCode().getValue().equalsIgnoreCase(languageCode)) {
                return t;
            }
        }
        return null;
    }

    public boolean hasTranslation(String languageCode) {
        return findTranslationByLanguage(languageCode) != null;
    }


    void _addTranslationDirect(AttributeOptionTranslation t) {
        if (t == null) return;
        this.translations.add(t);
    }

    void _removeTranslationDirect(AttributeOptionTranslation t) {
        if (t == null) return;
        this.translations.remove(t);
    }

    // ============================================================
    // == Getters
    // ============================================================

    public long getId() {
        return id;
    }

    public UUID getStableId() {
        return stableId;
    }

    public Code getCode() {
        return code;
    }

    public boolean isActive() {
        return active;
    }

    public DisplayOrder getDisplayOrder() {
        return displayOrder;
    }

    public AttributeDefinition getAttributeDefinition() {
        return attributeDefinition;
    }

    public Set<AttributeOptionTranslation> getTranslations() {
        return Set.copyOf(translations);
    }

    // ============================================================
    // == Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AttributeOption other = (AttributeOption) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // == Rules
    // ============================================================

    private static DisplayOrder mapOfDisplayOrder(Integer displayOrder) {
        return DisplayOrder.of(
                displayOrder,
                DISPLAY_SPEC,
                DISPLAY_KEYS,
                AttributeOption::ex
        );
    }

    private static Code mapOfCode(String raw) {
        return Code.of(
                raw,
                CODE_SPEC,
                CODE_KEYS,
                AttributeOption::ex
        );
    }
}
