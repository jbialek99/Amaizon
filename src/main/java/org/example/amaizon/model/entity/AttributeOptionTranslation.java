package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.AttributeOptionTranslationValidationException;
import org.example.amaizon.model.embeddable.all.DisplayName;
import org.example.amaizon.model.embeddable.all.LanguageCode;

import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.AttributeOptionTranslationValidation.*;

@Entity
@Table(
        name = "attribute_option_translations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attribute_option_translations_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_attribute_option_translations_lang", columnNames = {"attribute_option_id", "language_code"})
        }
)
public class AttributeOptionTranslation extends AuditableEntity {

    private static void ex(String key, Object... args) {
        throw new AttributeOptionTranslationValidationException(key, args);
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_option_id", nullable = false, updatable = false)
    private AttributeOption attributeOption;

    protected AttributeOptionTranslation() {
    }

    private AttributeOptionTranslation(String languageCode, String displayName, AttributeOption option) {
        this.languageCode = mapOfLanguageCode(languageCode);
        this.displayName = mapOfDisplayName(displayName);
        _attachAttributeOption(option);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static AttributeOptionTranslation create(String languageCode, String displayName, AttributeOption option) {
        return new AttributeOptionTranslation(languageCode, displayName, option);
    }

    void _attachAttributeOption(AttributeOption ao) {
        if(ao == null)
            ex(KEY_ATTRIBUTE_OPTION_NULL);

        if (this.attributeOption == ao) return;

        AttributeOption old = this.attributeOption;
        this.attributeOption = ao;

        if (old != null) {
            old._removeTranslationDirect(this);
        }

        ao._addTranslationDirect(this);
    }

    void _detachAttributeOption(){
        AttributeOption old = this.attributeOption;
        if (old == null) return;

        this.attributeOption = null;
        old._removeTranslationDirect(this);
    }

    // ============================================================
    // == Domain methods
    // ============================================================

    public void changeDisplayName(String displayName) {
        this.displayName = mapOfDisplayName(displayName);
    }

    public void changeAll(String displayName){
        changeDisplayName(displayName);
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

    public LanguageCode getLanguageCode() {
        return languageCode;
    }

    public DisplayName getDisplayName() {
        return displayName;
    }

    public AttributeOption getAttributeOption() {
        return attributeOption;
    }

    // ============================================================
    // == Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AttributeOptionTranslation other = (AttributeOptionTranslation) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }



    private static LanguageCode mapOfLanguageCode(String languageCode) {
        return LanguageCode.of(
                languageCode,
                LANG_CODE_SPEC,
                LANG_CODE_KEYS,
                AttributeOptionTranslation::ex
        );
    }

    private static DisplayName mapOfDisplayName(String displayName) {
        return DisplayName.of(
                displayName,
                DISPLAY_NAME_SPEC,
                DISPLAY_NAME_KEYS,
                AttributeOptionTranslation::ex
        );
    }
}
