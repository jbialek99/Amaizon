package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.AttributeGroupValidationException;
import org.example.amaizon.model.embeddable.all.Code;
import org.example.amaizon.model.embeddable.all.DisplayOrder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.AttributeGroupValidation.*;

@Entity
@Table(
        name = "attribute_groups",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attribute_groups_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_attribute_groups_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_attribute_groups_code", columnList = "code"),
                @Index(name = "idx_attribute_groups_stable_id", columnList = "stable_id")
        }
)
public class AttributeGroup extends AuditableEntity {

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

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "display_order", nullable = false)
    )
    private DisplayOrder displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @OneToMany(
            mappedBy = "attributeGroup",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private final Set<AttributeGroupTranslation> translations = new HashSet<>();

    @OneToMany(
            mappedBy = "attributeGroup",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private final Set<AttributeGroupItem> items = new HashSet<>();

    protected AttributeGroup() {
    }

    private AttributeGroup(String code, boolean active, Integer displayOrder) {
        this.code = mapOfCode(code);
        this.active = active;
        this.displayOrder = mapOfDisplayOrder(displayOrder);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static AttributeGroup create(String code, boolean active, Integer displayOrder) {
        return new AttributeGroup(code, active, displayOrder);
    }

    // ============================================================
    // == Domain methods
    // ============================================================

    public void changeCode(String code) {
        this.code = mapOfCode(code);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void changeDisplayOrder(Integer displayOrder) {
        this.displayOrder = mapOfDisplayOrder(displayOrder);
    }

    // ============================================================
    // == Relationship Management: Items
    // ============================================================

    public AttributeGroupItem addItem(AttributeDefinition definition, Integer displayOrder) {
        if (definition == null)
            ex(KEY_ATTRIBUTE_DEFINITION_NULL);

        AttributeGroupItem existing = findItemByDefinition(definition);
        if (existing != null)
            ex(KEY_ITEM_ALREADY_EXISTS);

        return AttributeGroupItem.create(this, definition, displayOrder);
    }

    public void removeItem(AttributeDefinition definition) {
        if (definition == null)
            ex(KEY_ATTRIBUTE_DEFINITION_NULL);

        AttributeGroupItem existing = findItemByDefinition(definition);
        if (existing == null)
            ex(KEY_ITEM_NOT_FOUND);
        existing._detachFromGroup();
    }

    public void removeItem(AttributeGroupItem item) {
        if (item == null) return;
        item._detachFromGroup();
    }

    public boolean hasDefinition(AttributeDefinition definition) {
        return findItemByDefinition(definition) != null;
    }

    public AttributeGroupItem findItemByDefinition(AttributeDefinition definition) {
        if (definition == null)
            return null;

        UUID targetStableId = definition.getStableId();
        for (AttributeGroupItem agi : items) {
            AttributeDefinition itDef = agi.getAttributeDefinition();
            if (itDef == null) continue;
            if (itDef == definition)
                return agi;
            if (targetStableId != null && Objects.equals(itDef.getStableId(), targetStableId))
                return agi;
        }
        return null;
    }

    void _addItemDirect(AttributeGroupItem item) {
        if (item == null) return;
        items.add(item);
    }

    void _removeItemDirect(AttributeGroupItem item) {
        if (item == null) return;
        items.remove(item);
    }


    // ============================================================
    // == Relationship Management: Translations
    // ============================================================

    public AttributeGroupTranslation createTranslation(String languageCode, String name) {
        if (languageCode == null) ex(KEY_LANGUAGE_CODE_NULL);
        AttributeGroupTranslation existing = findTranslationByLanguage(languageCode);
        if (existing != null) ex(KEY_TRANSLATION_ALREADY_EXISTS, languageCode);

        return AttributeGroupTranslation.create(languageCode, name, this);
    }

    public AttributeGroupTranslation updateTranslation(String languageCode, String name) {
        if (languageCode == null) ex(KEY_LANGUAGE_CODE_NULL);
        AttributeGroupTranslation existing = findTranslationByLanguage(languageCode);
        if (existing == null) ex(KEY_TRANSLATION_NOT_FOUND, languageCode);

        existing.changeAll(name);
        return existing;
    }

    public void removeTranslationByLanguage(String languageCode) {
        if (languageCode == null) ex(KEY_LANGUAGE_CODE_NULL);
        AttributeGroupTranslation existing = findTranslationByLanguage(languageCode);
        if (existing == null) ex(KEY_TRANSLATION_NOT_FOUND, languageCode);

        existing._detachAttributeGroup();
    }

    public void removeTranslation(AttributeGroupTranslation agt) {
        if (agt == null) return;
        agt._detachAttributeGroup();
    }

    public boolean hasTranslation(String languageCode) {
        return findTranslationByLanguage(languageCode) != null;
    }

    public AttributeGroupTranslation findTranslationByLanguage(String languageCode) {
        if (languageCode == null)
            return null;

        for (AttributeGroupTranslation t : translations) {
            if (languageCode.equalsIgnoreCase(t.getLanguageCode().getValue())) return t;
        }
        return null;
    }

    void _addTranslationDirect(AttributeGroupTranslation t) {
        if (t == null) return;
        translations.add(t);
    }

    void _removeTranslationDirect(AttributeGroupTranslation t) {
        if (t == null) return;
        translations.remove(t);
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

    public DisplayOrder getDisplayOrder() {
        return displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public Set<AttributeGroupTranslation> getTranslations() {
        return Set.copyOf(translations);
    }

    public Set<AttributeGroupItem> getItems() {
        return Set.copyOf(items);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AttributeGroup other = (AttributeGroup) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    private static void ex(String key, Object... args) {
        throw new AttributeGroupValidationException(key, args);
    }

    private static DisplayOrder mapOfDisplayOrder(Integer displayOrder) {
        return DisplayOrder.of(displayOrder, DISPLAY_SPEC, DISPLAY_KEYS, AttributeGroup::ex);
    }

    private static Code mapOfCode(String code) {
        return Code.of(code, CODE_SPEC, CODE_KEYS, AttributeGroup::ex);
    }
}
