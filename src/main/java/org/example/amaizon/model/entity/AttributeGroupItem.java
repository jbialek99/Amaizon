package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.AttributeGroupItemValidationException;
import org.example.amaizon.model.embeddable.all.DisplayOrder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.AttributeGroupItemValidation.*;

@Entity
@Table(
        name = "attribute_group_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attribute_group_items_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(
                        name = "uk_attribute_group_items_group_attribute",
                        columnNames = {"attribute_group_id", "attribute_definition_id"}
                )
        },
        indexes = {
                @Index(name = "idx_attribute_group_items_group", columnList = "attribute_group_id"),
                @Index(name = "idx_attribute_group_items_definition", columnList = "attribute_definition_id"),
                @Index(name = "idx_attribute_group_items_group_order", columnList = "attribute_group_id,display_order")
        }
)
public class AttributeGroupItem extends AuditableEntity {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", name = "stable_id", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "display_order", nullable = false)
    )
    private DisplayOrder displayOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_group_id", nullable = false, updatable = false)
    private AttributeGroup attributeGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_definition_id", nullable = false, updatable = false)
    private AttributeDefinition attributeDefinition;

    protected AttributeGroupItem() {
    }

    private AttributeGroupItem(AttributeGroup group, AttributeDefinition definition, Integer displayOrder) {
        _attachToGroup(validateGroup(group));
        _attachToDefinition(validateDefinition(definition));
        this.displayOrder = mapOfDisplayOrder(displayOrder);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static AttributeGroupItem create(AttributeGroup group, AttributeDefinition definition, Integer displayOrder) {
        return new AttributeGroupItem(group, definition, displayOrder);
    }

    // ============================================================
    // == Relationship consistency
    // ============================================================

    void _attachToGroup(AttributeGroup group) {
        if (group == null) ex(KEY_ATTRIBUTE_GROUP_NULL);
        if (this.attributeGroup == group) return;

        AttributeGroup old = this.attributeGroup;
        this.attributeGroup = group;

        if (old != null)
            old._removeItemDirect(this);

        group._addItemDirect(this);
    }

    void _detachFromGroup() {
        AttributeGroup old = this.attributeGroup;
        if (old == null) return;

        this.attributeGroup = null;
        old._removeItemDirect(this);
    }

    void _attachToDefinition(AttributeDefinition def) {
        if (def == null) ex(KEY_ATTRIBUTE_DEFINITION_NULL);
        this.attributeDefinition = def;
    }

    // ============================================================
    // == Domain
    // ============================================================

    public void changeDisplayOrder(Integer displayOrder) {
        this.displayOrder = mapOfDisplayOrder(displayOrder);
    }

    // ============================================================
    // == Getters
    // ============================================================

    public long getId() { return id; }

    public UUID getStableId() { return stableId; }

    public DisplayOrder getDisplayOrder() { return displayOrder; }


    public Integer getDisplayOrderValue() { return displayOrder.getValue(); }

    public AttributeGroup getAttributeGroup() { return attributeGroup; }

    public AttributeDefinition getAttributeDefinition() { return attributeDefinition; }


    // ============================================================
    // == Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AttributeGroupItem other = (AttributeGroupItem) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // == Rules
    // ============================================================

    private static AttributeGroup validateGroup(AttributeGroup group) {
        if (group == null) ex(KEY_ATTRIBUTE_GROUP_NULL);
        return group;
    }

    private static AttributeDefinition validateDefinition(AttributeDefinition def) {
        if (def == null) ex(KEY_ATTRIBUTE_DEFINITION_NULL);
        return def;
    }

    private static DisplayOrder mapOfDisplayOrder(Integer displayOrder) {
        return DisplayOrder.of(
                displayOrder,
                DISPLAY_SPEC,
                DISPLAY_KEYS,
                AttributeGroupItem::ex
        );
    }

    private static void ex(String key, Object... args) {
        throw new AttributeGroupItemValidationException(key, args);
    }
}
