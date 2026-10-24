package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.CategoryAttributeGroupException;
import org.example.amaizon.model.embeddable.all.DisplayOrder;
import org.example.amaizon.model.validators.validation.CategoryAttributeGroupValidation;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import static org.example.amaizon.model.validators.validation.CategoryAttributeGroupValidation.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(
        name = "category_attribute_groups",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_attribute_groups_stable_id", columnNames = "stable_id" ),
                @UniqueConstraint(name = "uk_category_attribute_groups_ids", columnNames = {"category_id", "attribute_group_id"})
        },
        indexes = {
                @Index(name = "idx_category_attribute_groups_category", columnList = "category_id" ),
                @Index(name = "idx_category_attribute_groups_attr_group", columnList = "attribute_group_id" ),
                @Index(name = "idx_category_attribute_groups_category_pos", columnList = "category_id,position" )
        }
)
public class CategoryAttributeGroup extends AuditableEntity {

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, updatable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_group_id", nullable = false, updatable = false)
    private AttributeGroup attributeGroup;


    @OneToMany(mappedBy = "categoryAttributeGroup", fetch = FetchType.LAZY)
    private final Set<CategoryAttributeAssignment> assignments = new HashSet<>();

    protected CategoryAttributeGroup() {
    }

    private CategoryAttributeGroup(Category category, AttributeGroup attributeGroup, int position) {
        _attachToCategory(category);
        _attachToAttributeGroup(attributeGroup);
        this.position = mapOfDisplayOrder(position);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static CategoryAttributeGroup create(Category category, AttributeGroup attributeGroup, int position) {
        return new CategoryAttributeGroup(category, attributeGroup, position);
    }

    // ============================================================
    // Relationship consistency
    // ============================================================

    void _attachToCategory(Category c) {
        if (c == null)
            ex(KEY_CATEGORY_NULL);
        if (this.category == c) return;

        Category old = this.category;
        this.category = c;

        if (old != null) old._removeCategoryAttributeGroupDirect(this);
        c._addCategoryAttributeGroupDirect(this);
    }

    void _attachToAttributeGroup(AttributeGroup g) {
        if (g == null)
            ex(KEY_ATTRIBUTE_GROUP_NULL);
        this.attributeGroup = g;
    }

    void _detachFromCategory() {
        Category old = this.category;
        if (old == null) return;

        this.category = null;
        old._removeCategoryAttributeGroupDirect(this);
    }

    void _addAssignmentDirect(CategoryAttributeAssignment a) {
        if (a == null) return;
        assignments.add(a);
    }

    void _removeAssignmentDirect(CategoryAttributeAssignment a) {
        if (a == null) return;
        assignments.remove(a);
    }

    // ============================================================
    // Domain
    // ============================================================

    public void changePosition(int position) {
        this.position = mapOfDisplayOrder(position);
    }

    public CategoryAttributeAssignment assign(
            AttributeDefinition def,
            boolean required,
            boolean filterable,
            boolean sortable,
            int position
    ) {
        if (category == null)
            ex(KEY_CATEGORY_NOT_FOUND);
        return category.assignAttribute(def, this, required, filterable, sortable, position);
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

    public Category getCategory() {
        return category;
    }

    public AttributeGroup getAttributeGroup() {
        return attributeGroup;
    }

    public Set<CategoryAttributeAssignment> getAssignments() {
        return Set.copyOf(assignments);
    }

    // ============================================================
    // Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CategoryAttributeGroup other = (CategoryAttributeGroup) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // Rules
    // ============================================================

    private static DisplayOrder mapOfDisplayOrder(int position) {
        return DisplayOrder.of(
                position,
                POSITION_SPEC,
                POSITION_KEYS,
                CategoryAttributeGroup::ex
        );
    }


    private static void ex(String key, Object... args) {
        throw new CategoryAttributeGroupException(key, args);
    }
}
