package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.CategoryValidationException;
import org.example.amaizon.model.embeddable.all.DisplayOrder;
import org.example.amaizon.model.embeddable.all.ImageUrl;
import org.example.amaizon.model.embeddable.all.Name;
import org.example.amaizon.model.enums.CategoryStatus;
import org.example.amaizon.model.validators.validation.CategoryValidation;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;

import static org.example.amaizon.model.validators.validation.CategoryValidation.*;

@Entity
@Table(
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_categories_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_categories_name", columnNames = "name")
        },
        indexes = {
                @Index(name = "idx_categories_parent", columnList = "parent_category_id"),
                @Index(name = "idx_categories_status", columnList = "status")
        }
)
public class Category extends AuditableEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", name = "stable_id", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false))
    private Name name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "image_url", nullable = false))
    private ImageUrl imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryStatus status;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "display_order", nullable = false))
    private DisplayOrder displayOrder;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Category> children = new ArrayList<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private final Set<Product> products = new HashSet<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<CategoryAttributeGroup> categoryAttributeGroups = new ArrayList<>();


    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private final Set<CategoryAttributeAssignment> attributeAssignments = new HashSet<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private final Set<CategoryCountry> categoryCountries = new HashSet<>();

    protected Category() {}

    private Category(String name, String imageUrl, Integer displayOrder) {
        this.name = mapOfName(name);
        this.imageUrl = mapOfImageUrl(imageUrl);
        this.status = CategoryStatus.DRAFT;
        this.displayOrder = mapOfDisplayOrder(displayOrder);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
        if (status == null) status = CategoryStatus.DRAFT;
    }

    public static Category createCategory(String name, String imageUrl, Integer displayOrder) {
        return new Category(name, imageUrl, displayOrder);
    }



    public void rename(String newName) {
        this.name = mapOfName(newName);
    }

    public void changeImageUrl(String newImageUrl) {
        this.imageUrl = mapOfImageUrl(newImageUrl);
    }

    public void changeStatus(CategoryStatus newStatus) {
        this.status = validateStatus(newStatus);
    }

    public void changeDisplayOrder(Integer newDisplayOrder) {
        this.displayOrder = mapOfDisplayOrder(newDisplayOrder);
    }

    public void changeParent(Category newParent) {
        if (newParent == null) {
            removeParent();
            return;
        }
        setParent(validateCategory(newParent));
    }

    public void removeParent() {
        deleteParent();
    }



    public CategoryAttributeAssignment assignAttribute(
            AttributeDefinition definition,
            CategoryAttributeGroup group,
            boolean required,
            boolean filterable,
            boolean sortable,
            int position
    ) {
        if (definition == null) ex(KEY_ATTRIBUTE_DEFINITION_NULL);


        if (hasAssignment(definition)) {
           ex(KEY_DEFINITION_ALREADY_EXIST);
        }

        if (group != null && group.getCategory() != this) {
            ex(KEY_GROUP_BELONGS_ANOTHER_CATEGORY);
        }

        return CategoryAttributeAssignment.create(this, definition, group, required, filterable, sortable, position);
    }


    public CategoryCountry assignCountry(Country country) {
        Country validated = validateCountry(country);

        boolean exists = categoryCountries.stream()
                .anyMatch(cc -> cc.getCountry().equals(validated));
        if (exists) ex(KEY_COUNTRY_ALREADY_ASSIGN);

        return CategoryCountry.create(this, validated);
    }

    public void unassignCountry(Country country) {
        Country validated = validateCountry(country);

        CategoryCountry existing = categoryCountries.stream()
                .filter(cc -> cc.getCountry().equals(validated))
                .findFirst()
                .orElse(null);

        if (existing == null) ex(KEY_COUNTRY_NOT_ASSIGNED);

        this.categoryCountries.remove(existing);
        validated._removeCategoryCountry(existing);
    }



    private void setParent(Category newParent) {
        if (this == newParent) ex(KEY_PARENT_CANNOT_BE_SELF);
        if (isDescendantOf(newParent, this)) ex(KEY_PARENT_CANNOT_BE_CHILD);

        if (Objects.equals(this.parent, newParent)) return;

        if (this.parent != null) {
            this.parent.children.remove(this);
        }

        this.parent = newParent;

        if (newParent != null) {
            newParent.children.add(this);
        }
    }

    private void deleteParent() {
        if (this.parent == null) return;
        this.parent.children.remove(this);
        this.parent = null;
    }

    private static boolean isDescendantOf(Category candidateParent, Category target) {
        if (candidateParent == null || target == null) return false;

        Category cursor = candidateParent;
        while (cursor != null) {
            if (cursor == target) return true;
            cursor = cursor.parent;
        }
        return false;
    }



    void _addProduct(Product product) {
        if (product == null) return;
        products.add(product);
    }

    void _removeProduct(Product product) {
        if (product == null) return;
        products.remove(product);
    }



    void _addCategoryCountry(CategoryCountry cc) {
        if (cc == null) return;
        categoryCountries.add(cc);
    }

    void _removeCategoryCountry(CategoryCountry cc) {
        if (cc == null) return;
        categoryCountries.remove(cc);
    }



    void _addCategoryAttributeGroupDirect(CategoryAttributeGroup g) {
        if (g == null) return;
        categoryAttributeGroups.add(g);
    }

    void _removeCategoryAttributeGroupDirect(CategoryAttributeGroup g) {
        if (g == null) return;
        categoryAttributeGroups.remove(g);
    }


    void _addAttributeAssignmentDirect(CategoryAttributeAssignment a) {
        if (a == null) return;
        attributeAssignments.add(a);
    }

    void _removeAttributeAssignmentDirect(CategoryAttributeAssignment a) {
        if (a == null) return;
        attributeAssignments.remove(a);
    }

    public CategoryAttributeAssignment findAssignmentByDefinition(AttributeDefinition def) {
        if (def == null) return null;
        UUID target = def.getStableId();

        for (CategoryAttributeAssignment a : attributeAssignments) {
            AttributeDefinition ad = a.getAttributeDefinition();
            if (ad == null) continue;
            if (ad == def) return a;
            if (target != null && Objects.equals(ad.getStableId(), target)) return a;
        }
        return null;
    }

    public boolean hasAssignment(AttributeDefinition def) {
        return findAssignmentByDefinition(def) != null;
    }


    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public Name getName() { return name; }
    public ImageUrl getImageUrl() { return imageUrl; }
    public CategoryStatus getStatus() { return status; }
    public DisplayOrder getDisplayOrder() { return displayOrder; }
    public Category getParent() { return parent; }

    public List<Category> getChildren() { return List.copyOf(children); }
    public Set<Product> getProducts() { return Set.copyOf(products); }
    public List<CategoryAttributeGroup> getCategoryAttributeGroups() { return List.copyOf(categoryAttributeGroups); }
    public Set<CategoryAttributeAssignment> getAttributeAssignments() { return Set.copyOf(attributeAssignments); }
    public Set<CategoryCountry> getCategoryCountries() { return Set.copyOf(categoryCountries); }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Category category = (Category) o;
        return Objects.equals(stableId, category.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }



    private static void ex(String key, Object... value) {
        throw new CategoryValidationException(key, value);
    }

    private static DisplayOrder mapOfDisplayOrder(Integer displayOrder) {
        return DisplayOrder.of(displayOrder, DISPLAY_ORDER_SPEC, DISPLAY_ORDER_KEYS, Category::ex);
    }

    private static Name mapOfName(String name) {
        return Name.of(name, CategoryValidation.NAME_SPEC, CategoryValidation.NAME_KEYS, Category::ex);
    }

    private static ImageUrl mapOfImageUrl(String imageUrl) {
        return ImageUrl.of(imageUrl, CategoryValidation.IMAGE_URL_SPEC, CategoryValidation.IMAGE_URL_KEYS, Category::ex);
    }

    private static CategoryStatus validateStatus(CategoryStatus status) {
        if (status == null) ex(KEY_STATUS_NULL);
        return status;
    }

    private static Category validateCategory(Category category) {
        if (category == null) ex(KEY_CATEGORY_IS_NULL);
        return category;
    }

    private static Country validateCountry(Country country) {
        if (country == null) ex(KEY_COUNTRY_IS_NULL);
        return country;
    }
}
