package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.ProductAttributeValueValidationException;
import org.example.amaizon.model.embeddable.attributeDefinition.AttributeValue;
import org.example.amaizon.model.enums.AttributeDefinitionType;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.ProductAttributeValueValidation.*;

@Entity
@Table(
        name = "product_attribute_values",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_attribute_values_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_product_attribute_values_ids", columnNames = {"product_id", "attribute_definition_id", "position"})
        },
        indexes = {
                @Index(name = "idx_pav_product_attr", columnList = "product_id,attribute_definition_id"),
                @Index(name = "idx_pav_attr_value_number", columnList = "attribute_definition_id,value_number"),
                @Index(name = "idx_pav_attr_value_code", columnList = "attribute_definition_id,value_code"),
                @Index(name = "idx_pav_attr_value_string", columnList = "attribute_definition_id,value_string")
        }
)
public class ProductAttributeValue extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", name = "stable_id", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @Column(nullable = false)
    private int position;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valueString", column = @Column(name = "value_string", length = 500)),
            @AttributeOverride(name = "valueNumber", column = @Column(name = "value_number", precision = 16, scale = 2)),
            @AttributeOverride(name = "valueBoolean", column = @Column(name = "value_boolean")),
            @AttributeOverride(name = "valueTimestamp", column = @Column(name = "value_timestamp")),
            @AttributeOverride(name = "valueCode", column = @Column(name = "value_code", length = 100))
    })
    private AttributeValue value;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_definition_id", nullable = false, updatable = false)
    private AttributeDefinition attributeDefinition;

    protected ProductAttributeValue() {}

    private ProductAttributeValue(Product product, AttributeDefinition attributeDefinition, AttributeValue value, int position) {
        this.product = validateProduct(product);
        this.attributeDefinition = validateAttributeDefinition(attributeDefinition);
        this.value = validateValue(value);

        this.position = validatePosition(position, this.attributeDefinition);

        validateValueMatchesDefinition(this.attributeDefinition.getType(), this.value);

        if (this.attributeDefinition.getType() == AttributeDefinitionType.DECIMAL
                || this.attributeDefinition.getType() == AttributeDefinitionType.INT) {

            if (this.value.getValueNumber() != null) {
                applyNumericConstraintsIfAny(this.attributeDefinition, this.value.getValueNumber());
            }
        }
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static ProductAttributeValue create(Product product, AttributeDefinition attributeDefinition, AttributeValue value, int position) {
        return new ProductAttributeValue(product, attributeDefinition, value, position);
    }

    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public int getPosition() { return position; }
    public AttributeValue getValue() { return value; }
    public Product getProduct() { return product; }
    public AttributeDefinition getAttributeDefinition() { return attributeDefinition; }

    private static Product validateProduct(Product product) {
        if (product == null) ex(KEY_PRODUCT_NULL);
        return product;
    }

    private static AttributeDefinition validateAttributeDefinition(AttributeDefinition def) {
        if (def == null) ex(KEY_ATTRIBUTE_DEFINITION_NULL);
        return def;
    }

    private static AttributeValue validateValue(AttributeValue value) {
        if (value == null) ex(VALUE_NOT_SET);
        value.validateOrThrow();
        return value;
    }

    private static int validatePosition(int position, AttributeDefinition def) {
        if (position < 0) ex(KEY_POSITION_NEGATIVE);
        if (!def.isMultiValue() && position != 0) ex(KEY_POSITION_NOT_ALLOWED);
        return position;
    }

    private static void validateValueMatchesDefinition(AttributeDefinitionType type, AttributeValue v) {
        boolean ok = switch (type) {
            case TEXT -> v.getValueString() != null;
            case CODE -> v.getValueCode() != null;
            case BOOLEAN -> v.getValueBoolean() != null;
            case TIMESTAMP -> v.getValueTimestamp() != null;
            case INT, DECIMAL -> v.getValueNumber() != null;
        };
        if (!ok) ex(KEY_VALUE_TYPE_MISMATCH);
    }

    private static void applyNumericConstraintsIfAny(AttributeDefinition def, BigDecimal value) {
        var nc = def.getNumericConstraints();
        if (nc == null || !nc.isDefined()) return;

        if (nc.getMinimum() != null && value.compareTo(nc.getMinimum()) < 0) ex(VALUE_NUMBER_TOO_LOW, nc.getMinimum());
        if (nc.getMaximum() != null && value.compareTo(nc.getMaximum()) > 0) ex(VALUE_NUMBER_TOO_HIGH, nc.getMaximum());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProductAttributeValue other = (ProductAttributeValue) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override public int hashCode() { return Objects.hash(stableId); }

    private static void ex(String key, Object... args) {
        throw new ProductAttributeValueValidationException(key, args);
    }
}
