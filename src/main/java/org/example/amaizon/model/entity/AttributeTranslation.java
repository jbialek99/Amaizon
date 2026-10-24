package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.AttributeTranslationValidationException;
import org.example.amaizon.model.embeddable.all.Description;
import org.example.amaizon.model.embeddable.all.DisplayName;
import org.example.amaizon.model.embeddable.all.LanguageCode;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.AttributeTranslationValidation.*;

@Entity
@Table(
        name = "attribute_translations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attribute_translations_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_attribute_translations_lang", columnNames = {"attribute_definition_id", "language_code"})
        },
        indexes = {
                @Index(name = "idx_attribute_translations_lang_code", columnList = "language_code"),
                @Index(name = "idx_attribute_translations_attr_def", columnList = "attribute_definition_id")
        }
)
public class AttributeTranslation extends AuditableEntity {

    private static void ex(String key, Object... args) {
        throw new AttributeTranslationValidationException(key, args);
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
            column = @Column(name = "language_code", nullable = false, updatable = false)
    )
    private LanguageCode languageCode;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "name", nullable = false)
    )
    private DisplayName displayName;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    )
    private Description description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_definition_id", nullable = false, updatable = false)
    private AttributeDefinition attributeDefinition;

    protected AttributeTranslation() {
    }

    private AttributeTranslation(
            String languageCode,
            String displayName,
            String description,
            AttributeDefinition attributeDefinition
    ) {
        this.languageCode = mapOfLanguageCode(languageCode);
        this.displayName = mapOfDisplayName(displayName);
        this.description = mapOfDescription(description);
        _attachToAttributeDefinition(validateAttributeDefinition(attributeDefinition));
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static AttributeTranslation create(
            String languageCode,
            String displayName,
            String description,
            AttributeDefinition attributeDefinition
    ) {
        return new AttributeTranslation(languageCode, displayName, description, attributeDefinition);
    }

    void _attachToAttributeDefinition(AttributeDefinition newDef) {
        if (newDef == null) ex(KEY_ATTRIBUTE_DEFINITION_NULL);
        if (this.attributeDefinition == newDef) return;

        AttributeDefinition old = this.attributeDefinition;
        this.attributeDefinition = newDef;

        if (old != null) old._removeTranslationDirect(this);
        newDef._addTranslationDirect(this);
    }

    void _detachFromAttributeDefinition() {
        AttributeDefinition old = this.attributeDefinition;
        if (old == null) return;

        this.attributeDefinition = null;
        old._removeTranslationDirect(this);
    }

    public void changeDisplayName(String displayName) {
        this.displayName = mapOfDisplayName(displayName);
    }

    public void changeDescription(String description) {
        this.description = mapOfDescription(description);
    }

    public void changeAll(String displayName, String description) {
        changeDisplayName(displayName);
        changeDescription(description);
    }

    public String getLanguageCode() {
        return languageCode.getValue();
    }

    public DisplayName getDisplayName() {
        return displayName;
    }


    public Description getDescription() {
        return description;
    }


    public AttributeDefinition getAttributeDefinition() {
        return attributeDefinition;
    }

    public UUID getStableId() {
        return stableId;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AttributeTranslation other = (AttributeTranslation) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    private static AttributeDefinition validateAttributeDefinition(AttributeDefinition def) {
        if (def == null) ex(KEY_ATTRIBUTE_DEFINITION_NULL);
        return def;
    }

    private static LanguageCode mapOfLanguageCode(String languageCode) {
        return LanguageCode.of(languageCode, LANG_CODE_SPEC, LANG_CODE_KEYS, AttributeTranslation::ex);
    }

    private static DisplayName mapOfDisplayName(String displayName) {
        return DisplayName.of(displayName, DISPLAY_NAME_SPEC, DISPLAY_NAME_KEYS, AttributeTranslation::ex);
    }

    private static Description mapOfDescription(String description) {
        return Description.of(description, DESCRIPTION_SPEC, DESCRIPTION_KEYS, AttributeTranslation::ex);
    }
}
