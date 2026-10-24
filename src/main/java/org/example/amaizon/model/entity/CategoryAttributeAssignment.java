package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.CategoryAttributeAssignmentValidationException;
import org.example.amaizon.model.embeddable.all.DisplayOrder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.CategoryAttributeAssignmentValidation.*;

@Entity
@Table(
        name = "category_attribute_assignments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_attribute_assignments_stable_id", columnNames = "stable_id" ),
                @UniqueConstraint(name = "uk_category_attribute_assignments_ids", columnNames = {"category_id", "attribute_definition_id"})
        },
        indexes = {
                @Index(name = "idx_category_attribute_assignments_category", columnList = "category_id" ),
                @Index(name = "idx_category_attribute_assignments_attribute", columnList = "attribute_definition_id" ),
                @Index(name = "idx_category_attribute_assignments_group", columnList = "category_attribute_group_id" ),
                @Index(name = "idx_category_attribute_assignments_cat_group_pos",
                        columnList = "category_id,category_attribute_group_id,position" )
        }
)
public class CategoryAttributeAssignment extends AuditableEntity {

    private static void ex(String key, Object... args) {
        throw new CategoryAttributeAssignmentValidationException(key, args);
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
            column = @Column(name = "position", nullable = false)
    )
    private DisplayOrder position;

    @Column(name = "is_required", nullable = false)
    private boolean required;

    @Column(name = "is_filterable", nullable = false)
    private boolean filterable;

    @Column(name = "is_sortable", nullable = false)
    private boolean sortable;

    // ============================================================
    // Relationships
    // ============================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, updatable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_definition_id", nullable = false, updatable = false)
    private AttributeDefinition attributeDefinition;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_attribute_group_id", updatable = false)
    private CategoryAttributeGroup categoryAttributeGroup;

    // ============================================================
    // Constructors / Factory
    // ============================================================

    protected CategoryAttributeAssignment() {
    }

    private CategoryAttributeAssignment(
            Category category,
            AttributeDefinition attributeDefinition,
            CategoryAttributeGroup group,
            boolean required,
            boolean filterable,
            boolean sortable,
            int position
    ) {
        if (group != null && group.getCategory() != category) {
            ex(KEY_CATEGORY_MISMATCH);
        }

        _attachToCategory(category);
        _attachToAttributeDefinition(attributeDefinition);
        _attachToGroupNullable(group);

        this.required = required;
        this.filterable = filterable;
        this.sortable = sortable;
        this.position = mapOfDisplayOrder(position);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static CategoryAttributeAssignment create(
            Category category,
            AttributeDefinition attributeDefinition,
            CategoryAttributeGroup group,
            boolean required,
            boolean filterable,
            boolean sortable,
            int position
    ) {
        return new CategoryAttributeAssignment(category, attributeDefinition, group, required, filterable, sortable, position);
    }

    // ============================================================
    // Relationship consistency
    // ============================================================

    void _attachToCategory(Category c) {
        if (c == null) ex(KEY_CATEGORY_NULL);
        if (this.category == c) return;

        Category old = this.category;
        this.category = c;

        if (old != null) old._removeAttributeAssignmentDirect(this);
        c._addAttributeAssignmentDirect(this);
    }

    void _detachFromCategory() {
        Category old = this.category;
        if (old == null) return;

        this.category = null;
        old._removeAttributeAssignmentDirect(this);
    }

    void _attachToAttributeDefinition(AttributeDefinition def) {
        if (def == null) ex(KEY_ATTRIBUTE_DEFINITION_NULL);
        this.attributeDefinition = def;
    }

    void _attachToGroupNullable(CategoryAttributeGroup g) {
        if (this.categoryAttributeGroup == g) return;

        CategoryAttributeGroup old = this.categoryAttributeGroup;
        this.categoryAttributeGroup = g;

        if (old != null) old._removeAssignmentDirect(this);
        if (g != null) g._addAssignmentDirect(this);
    }

    void _detachFromGroup() {
        CategoryAttributeGroup old = this.categoryAttributeGroup;
        if (old == null) return;

        this.categoryAttributeGroup = null;
        old._removeAssignmentDirect(this);
    }

    // ============================================================
    // Domain methods
    // ============================================================

    public void changeFlags(boolean required, boolean filterable, boolean sortable) {
        this.required = required;
        this.filterable = filterable;
        this.sortable = sortable;
    }

    public void changePosition(int position) {
        this.position = mapOfDisplayOrder(position);
    }

    // ============================================================
    // Getters
    // ============================================================

    public long getId() {
        return id;
    }

    public UUID getStableId() {
        return stableId;
    }

    public DisplayOrder getPosition() {
        return position;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isFilterable() {
        return filterable;
    }

    public boolean isSortable() {
        return sortable;
    }

    public Category getCategory() {
        return category;
    }

    public AttributeDefinition getAttributeDefinition() {
        return attributeDefinition;
    }

    public CategoryAttributeGroup getCategoryAttributeGroup() {
        return categoryAttributeGroup;
    }

    // ============================================================
    // Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CategoryAttributeAssignment other = (CategoryAttributeAssignment) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    private static DisplayOrder mapOfDisplayOrder(int position) {
        return DisplayOrder.of(
                position,
                POSITION_SPEC,
                POSITION_KEYS,
                CategoryAttributeAssignment::ex);
    }
}
