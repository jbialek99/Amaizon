package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.AttributeGroupTranslationValidationException;
import org.example.amaizon.model.embeddable.all.DisplayName;
import org.example.amaizon.model.embeddable.all.LanguageCode;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.AttributeGroupTranslationValidation.*;

@Entity
@Table(
        name = "attribute_group_translations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attribute_group_translations_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_attribute_group_translations_lang", columnNames = {"attribute_group_id", "language_code"})
        },
        indexes = {
                @Index(name = "idx_attribute_group_translations_group_lang", columnList = "attribute_group_id,language_code")
        }
)
public class AttributeGroupTranslation extends AuditableEntity {


    // ============================================================
    // == Fields ==
    // ============================================================

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
            column = @Column(name = "display_name", nullable = false)
    )
    private DisplayName displayName;

    // ============================================================
    // == Relationships ==
    // ============================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_group_id", nullable = false, updatable = false)
    private AttributeGroup attributeGroup;

    // ============================================================
    // == Constructors ==
    // ============================================================

    protected AttributeGroupTranslation() {
    }

    private AttributeGroupTranslation(String languageCode, String displayName, AttributeGroup ag) {
        this.languageCode = mapOfLanguageCode(languageCode);
        this.displayName = mapOfDisplayName(displayName);
        _attachAttributeGroup(ag);
    }

    // ============================================================
    // == JPA Hooks ==
    // ============================================================

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    // ============================================================
    // == Factory ==
    // ============================================================

    public static AttributeGroupTranslation create(String languageCode, String displayName, AttributeGroup ag) {
        return new AttributeGroupTranslation(languageCode, displayName, ag);
    }

    // ============================================================
    // == Relationship consistency ==
    // ============================================================

    void _attachAttributeGroup(AttributeGroup ag) {
        if (ag == null)
            ex(KEY_ATTRIBUTE_GROUP_NULL);

        if (this.attributeGroup == ag) return;

        AttributeGroup old = this.attributeGroup;
        this.attributeGroup = ag;

        if (old != null)
            old._removeTranslationDirect(this);


        ag._addTranslationDirect(this);
    }


    void _detachAttributeGroup() {
       AttributeGroup old = this.attributeGroup;
       if(old == null) return;

       this.attributeGroup = null;
        old._removeTranslationDirect(this);
    }


    // ============================================================
    // == Domain methods ==
    // ============================================================


    public void changeDisplayName(String displayName) {
        this.displayName = mapOfDisplayName(displayName);
    }

    public void changeAll(String displayName) {
        changeDisplayName(displayName);
    }

    // ============================================================
    // == Getters ==
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

    public AttributeGroup getAttributeGroup() {
        return attributeGroup;
    }

    // ============================================================
    // == Equality ==
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AttributeGroupTranslation other = (AttributeGroupTranslation) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // == Rules ==
    // ============================================================


    private static LanguageCode mapOfLanguageCode(String raw) {
        return LanguageCode.of(
                raw,
                LANG_CODE_SPEC,
                LANG_CODE_KEYS,
                AttributeGroupTranslation::ex
        );
    }

    private static DisplayName mapOfDisplayName(String raw) {
        return DisplayName.of(
                raw,
                DISPLAY_NAME_SPEC,
                DISPLAY_NAME_KEYS,
                AttributeGroupTranslation::ex
        );
    }

    private static void ex(String key, Object... args) {
        throw new AttributeGroupTranslationValidationException(key, args);
    }
}
