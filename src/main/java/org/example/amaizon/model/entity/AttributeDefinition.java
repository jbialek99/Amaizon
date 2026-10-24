package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.AttributeDefinitionValidationException;
import org.example.amaizon.model.embeddable.all.Code;
import org.example.amaizon.model.embeddable.attributeDefinition.NumericConstraints;
import org.example.amaizon.model.enums.AttributeDefinitionType;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.AttributeDefinitionValidation.*;

@Entity
@Table(
        name = "attribute_definitions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attribute_definitions_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_attribute_definitions_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_attribute_definitions_type", columnList = "type"),
                @Index(name = "idx_attribute_definitions_filterable", columnList = "is_filterable"),
                @Index(name = "idx_attribute_definitions_sortable", columnList = "is_sortable"),
                @Index(name = "idx_attribute_definitions_unit", columnList = "unit_id")
        }
)
public class AttributeDefinition extends AuditableEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition="uuid", name = "stable_id", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "code", nullable = false)
    )
    private Code code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttributeDefinitionType type;

    @Column(name = "is_multi_value", nullable = false)
    private boolean isMultiValue;

    @Column(name = "is_filterable", nullable = false)
    private boolean isFilterable;

    @Column(name = "is_sortable", nullable = false)
    private boolean isSortable;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "minimum", column = @Column(name = "min_value", precision = 16, scale = 2)),
            @AttributeOverride(name = "maximum", column = @Column(name = "max_value", precision = 16, scale = 2)),
            @AttributeOverride(name = "decimalScale", column = @Column(name = "decimal_scale"))
    })
    private NumericConstraints numericConstraints;

    // ============================================================
    // == Relationships
    // ============================================================

    @OneToMany(
            mappedBy = "attributeDefinition",
            orphanRemoval = true,
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private Set<ProductAttributeValue> productAttributeValues = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @OneToMany(
            mappedBy = "attributeDefinition",
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.ALL
    )
    private Set<AttributeTranslation> translations = new HashSet<>();

    @OneToMany(
            mappedBy = "attributeDefinition",
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.ALL
    )
    private Set<AttributeOption> attributeOptions = new HashSet<>();

    @OneToMany(mappedBy = "attributeDefinition", fetch = FetchType.LAZY)
    private final Set<CategoryAttributeAssignment> categoryAssignments = new HashSet<>();

    // ============================================================
    // == Constructors
    // ============================================================


    protected AttributeDefinition() {
    }

    private AttributeDefinition(
            String code,
            AttributeDefinitionType type,
            boolean isMultiValue,
            boolean isFilterable,
            boolean isSortable,
            BigDecimal minimum,
            BigDecimal maximum,
            Integer decimalScale,
            Unit unit
    ) {
        this.type = validateType(type);
        this.code = mapOfCode(code);
        this.isMultiValue = isMultiValue;
        this.isFilterable = isFilterable;
        this.isSortable = isSortable;

        applyNumericAndUnit(minimum, maximum, decimalScale, unit);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static AttributeDefinition create(
            String code,
            AttributeDefinitionType type,
            boolean isMultiValue,
            boolean isFilterable,
            boolean isSortable,
            BigDecimal minimum,
            BigDecimal maximum,
            Integer decimalScale,
            Unit unit
    ) {
        return new AttributeDefinition(code, type, isMultiValue, isFilterable, isSortable, minimum, maximum, decimalScale, unit);
    }


    // ============================================================
    // == Relationship Management: Attribute Options
    // ============================================================

    public AttributeOption createAttributeOption(String code, boolean status, Integer displayOrder) {
        if (code == null)
            ex(KEY_ATTRIBUTE_OPTION_CODE_NULL);
        AttributeOption existing = findAttributeOptionByCode(code);
        if (existing != null) {
            ex(KEY_OPTION_ALREADY_EXISTS, code);
        }
        return AttributeOption.create(code, status, displayOrder, this);
    }

    public AttributeOption updateAttributeOption(String oldCode, String newCode, boolean status, Integer displayOrder) {
        if (oldCode == null)
            ex(KEY_ATTRIBUTE_OPTION_CODE_NULL);
        AttributeOption toUpdate = findAttributeOptionByCode(oldCode);
        if (toUpdate == null) {
            ex(KEY_OPTION_NOT_EXISTS, oldCode);
        }
        toUpdate.changeAll(newCode, displayOrder, status);
        return toUpdate;
    }


    public void removeAttributeOptionByCode(String code) {
        if (code == null)
            ex(KEY_ATTRIBUTE_OPTION_CODE_NULL);
        AttributeOption existing = findAttributeOptionByCode(code);
        if (existing == null) {
            ex(KEY_OPTION_NOT_EXISTS, code);
        }
        existing._detachAttributeDefinition();
    }

    public void removeAttributeOption(AttributeOption ao) {
        if (ao == null) return;
        ao._detachAttributeDefinition();
    }

    public boolean hasOption(String code) {
        return findAttributeOptionByCode(code) != null;
    }

    public AttributeOption findAttributeOptionByCode(String code) {
        if (code == null)
            return null;

        for (AttributeOption o : attributeOptions) {
            if (o.getCode().getValue().equalsIgnoreCase(code))
                return o;
        }
        return null;
    }

    void _addAttributeOptionDirect(AttributeOption option) {
        if (option == null) return;
        this.attributeOptions.add(option);
    }

    void _removeAttributeOptionDirect(AttributeOption option) {
        if (option == null) return;
        this.attributeOptions.remove(option);
    }


    // ============================================================
    // == Relationship Management: Translations
    // ============================================================

    public AttributeTranslation createTranslation(String languageCode, String name, String description) {
        if (languageCode == null) {
            ex(KEY_LANGUAGE_CODE_NULL);
        }
        AttributeTranslation existing = findTranslationByLanguage(languageCode);
        if (existing != null) {
            ex(KEY_TRANSLATION_ALREADY_EXISTS, languageCode);
        }
        return AttributeTranslation.create(languageCode, name, description, this);
    }

    public AttributeTranslation updateTranslation(String languageCode, String name, String description) {
        if (languageCode == null) {
            ex(KEY_LANGUAGE_CODE_NULL);
        }
        AttributeTranslation toUpdate = findTranslationByLanguage(languageCode);
        if (toUpdate == null) {
            ex(KEY_TRANSLATION_NOT_EXISTS, languageCode);
        }
        toUpdate.changeAll(name, description);
        return toUpdate;
    }

    public void removeTranslationByLanguage(String languageCode) {
        if (languageCode == null) {
            ex(KEY_LANGUAGE_CODE_NULL);
        }
        AttributeTranslation existing = findTranslationByLanguage(languageCode);
        if (existing == null) {
            ex(KEY_TRANSLATION_NOT_EXISTS, languageCode);
        }
        existing._detachFromAttributeDefinition();
    }

    public void removeTranslation(AttributeTranslation translation) {
        if (translation == null) return;
        translation._detachFromAttributeDefinition();
    }

    public boolean hasTranslation(String languageCode) {
        return findTranslationByLanguage(languageCode) != null;
    }

    public AttributeTranslation findTranslationByLanguage(String languageCode) {
        if (languageCode == null)
            return null;

        for (AttributeTranslation t : this.translations) {
            if (t.getLanguageCode().equalsIgnoreCase(languageCode)) {
                return t;
            }
        }
        return null;
    }

    void _addTranslationDirect(AttributeTranslation translation) {
        if (translation == null) return;
        this.translations.add(translation);
    }

    void _removeTranslationDirect(AttributeTranslation translation) {
        if (translation == null) return;
        this.translations.remove(translation);
    }


    // ============================================================
    // == Relationship Management: Product Attribute Values
    // ============================================================

    void _addProductAttributeValueDirect(ProductAttributeValue pav) {
        if (pav == null) return;
        this.productAttributeValues.add(pav);
    }

    void _removeProductAttributeValueDirect(ProductAttributeValue pav) {
        if (pav == null) return;
        this.productAttributeValues.remove(pav);
    }

    // ============================================================
    // == Domain Methods (update state)
    // ============================================================

    public void updateAll(
            String code,
            AttributeDefinitionType type,
            boolean isMultiValue,
            boolean isFilterable,
            boolean isSortable,
            BigDecimal minimum,
            BigDecimal maximum,
            Integer decimalScale,
            Unit unit
    ) {
        updateCode(code);
        updateType(type);
        updateMultiValue(isMultiValue);
        updateFilterable(isFilterable);
        updateSortable(isSortable);

        if (isNumericType(this.type)) {
            updateUnit(unit);
            updateNumericConstraints(minimum, maximum, decimalScale);
        } else {
            this.unit = null;
            this.numericConstraints = null;
        }
    }

    public void updateCode(String code) {
        this.code = mapOfCode(code);
    }

    public void updateType(AttributeDefinitionType newType) {
        if (this.type == newType) return;

        AttributeDefinitionType validated = validateType(newType);

        if (this.numericConstraints != null) {
            Integer scale = this.numericConstraints.getDecimalScale();
            validateScaleForNumericType(scale, validated);
        }

        this.type = validated;

        if (!isNumericType(this.type)) {
            this.numericConstraints = null;
            this.unit = null;
        }
    }

    public void updateMultiValue(boolean isMultiValue) {
        this.isMultiValue = isMultiValue;
    }

    public void updateFilterable(boolean filterable) {
        this.isFilterable = filterable;
    }

    public void updateSortable(boolean sortable) {
        this.isSortable = sortable;
    }

    public void updateUnit(Unit unit) {
        if (!isNumericType(this.type)) {
            this.unit = null;
            return;
        }
        this.unit = validateUnitNullable(unit);
    }

    public void updateNumericConstraints(BigDecimal minimum, BigDecimal maximum, Integer decimalScale) {
        requireNumericType();

        if (minimum == null && maximum == null && decimalScale == null) {
            this.numericConstraints = null;
            return;
        }

        validateScaleForNumericType(decimalScale, this.type);
        this.numericConstraints = NumericConstraints.of(minimum, maximum, decimalScale);
    }

    public void removeNumericConstraints() {
        this.numericConstraints = null;
    }

    public boolean isNumeric() {
        return isNumericType(this.type);
    }

    // Getters
    public long getId() {
        return id;
    }

    public UUID getStableId() {
        return stableId;
    }

    public Code getCode() {
        return code;
    }

    public AttributeDefinitionType getType() {
        return type;
    }

    public boolean isMultiValue() {
        return isMultiValue;
    }

    public boolean isFilterable() {
        return isFilterable;
    }

    public boolean isSortable() {
        return isSortable;
    }

    public NumericConstraints getNumericConstraints() {
        return numericConstraints;
    }

    public Unit getUnit() {
        return unit;
    }

    public Set<AttributeTranslation> getTranslations() {
        return Set.copyOf(translations);
    }

    public Set<AttributeOption> getAttributeOptions() {
        return Set.copyOf(attributeOptions);
    }

    public Set<ProductAttributeValue> getProductAttributeValues() {
        return Set.copyOf(productAttributeValues);
    }

    public Set<CategoryAttributeAssignment> getCategoryAssignments() {
        return Set.copyOf(categoryAssignments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AttributeDefinition other = (AttributeDefinition) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    private static Code mapOfCode(String code) {
        return Code.of(code, CODE_SPEC, CODE_KEYS, AttributeDefinition::ex);
    }

    private void applyNumericAndUnit(BigDecimal minimum, BigDecimal maximum, Integer decimalScale, Unit unit) {
        if (!isNumericType(this.type)) {
            this.numericConstraints = null;
            this.unit = null;
            if (minimum != null || maximum != null || decimalScale != null || unit != null) {
                ex(KEY_NOT_ALLOWED_FOR_TYPE);
            }
            return;
        }

        this.unit = validateUnitNullable(unit);

        if (minimum != null || maximum != null || decimalScale != null) {
            validateScaleForNumericType(decimalScale, this.type);
            this.numericConstraints = NumericConstraints.of(minimum, maximum, decimalScale);
        } else {
            this.numericConstraints = null;
        }
    }

    private static Unit validateUnitNullable(Unit unit) {
        if (unit == null) return null;
        if (!unit.isActive()) ex(KEY_UNIT_INACTIVE);
        return unit;
    }

    private static AttributeDefinitionType validateType(AttributeDefinitionType type) {
        if (type == null) ex(KEY_TYPE_NULL);
        return type;
    }

    private void requireNumericType() {
        if (!isNumericType(this.type)) ex(KEY_NOT_ALLOWED_FOR_TYPE);
    }

    private static boolean isNumericType(AttributeDefinitionType type) {
        return type == AttributeDefinitionType.INT || type == AttributeDefinitionType.DECIMAL;
    }

    private static void validateScaleForNumericType(Integer decimalScale, AttributeDefinitionType type) {
        if (type == AttributeDefinitionType.DECIMAL) {
            if (decimalScale == null) ex(KEY_DECIMAL_SCALE_REQUIRED_FOR_DECIMAL);
        }
        if (type == AttributeDefinitionType.INT) {
            if (decimalScale != null && decimalScale != 0) ex(KEY_DECIMAL_SCALE_NOT_ALLOWED_FOR_INT);
        }
    }

    private static void ex(String key, Object... args) {
        throw new AttributeDefinitionValidationException(key, args);
    }
}
