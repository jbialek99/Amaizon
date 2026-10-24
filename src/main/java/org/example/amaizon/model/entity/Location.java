package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.LocationValidationException;
import org.example.amaizon.model.embeddable.all.Name;
import org.example.amaizon.model.enums.LocationType;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.LocationValidation.*;


@Entity
@Table(
        name = "locations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_locations_stable_id", columnNames = "stable_id")
        },
        indexes = {
                @Index(name = "idx_locations_country", columnList = "country_id"),
                @Index(name = "idx_locations_parent", columnList = "parent_id")
        }
)
public class Location extends AuditableEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", name = "stable_id", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "name", nullable = false)
    )
    private Name name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LocationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Location parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private final Set<Location> children = new HashSet<>();

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    private final Set<Workplace> workplaces = new HashSet<>();

    protected Location() {
    }

    private Location(Country country, String name, LocationType type, Location parent) {
        _attachToCountry(validateCountry(country));
        this.name = mapOfName(name);
        this.type = validateType(type);
        _attachToParent(parent);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static Location create(Country country, String name, LocationType type, Location parent) {
        return new Location(country, name, type, parent);
    }

    // ============================================================
    // == Domain methods
    // ============================================================

    public void changeName(String name) {
        this.name = mapOfName(name);
    }

    public void changeType(LocationType type) {
        this.type = validateType(type);
    }

    public void changeParent(Location newParent) {
        _attachToParent(newParent);
    }

    public void updateAll(String name, LocationType type, Location parent) {
        changeName(name);
        changeType(type);
        changeParent(parent);
    }

    // ============================================================
    // == Relationship Management: Country
    // ============================================================

    void _attachToCountry(Country newCountry) {
        if (newCountry == null) ex(KEY_COUNTRY_IS_NULL);
        if (this.country == newCountry) return;

        Country old = this.country;
        this.country = newCountry;

        if (old != null) old._removeLocationDirect(this);
        newCountry._addLocationDirect(this);
    }

    void _detachFromCountry() {
        Country old = this.country;
        if (old == null) return;

        this.country = null;
        old._removeLocationDirect(this);
    }

    // ============================================================
    // == Relationship Management: Parent
    // ============================================================

    private void _attachToParent(Location newParent) {
        if (this.parent == newParent) return;

        Location old = this.parent;
        this.parent = newParent;

        if (old != null) old._removeChildDirect(this);
        if (newParent != null) newParent._addChildDirect(this);
    }

    void _addChildDirect(Location child) {
        if (child == null) return;
        this.children.add(child);
    }

    void _removeChildDirect(Location child) {
        if (child == null) return;
        this.children.remove(child);
    }

    // ============================================================
    // == Relationship Management: Workplaces (inverse)
    // ============================================================

    void _addWorkplaceDirect(Workplace workplace) {
        if (workplace == null) return;
        this.workplaces.add(workplace);
    }

    void _removeWorkplaceDirect(Workplace workplace) {
        if (workplace == null) return;
        this.workplaces.remove(workplace);
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

    public Country getCountry() {
        return country;
    }

    public Name getName() {
        return name;
    }

    public LocationType getType() {
        return type;
    }

    public Location getParent() {
        return parent;
    }

    public Set<Location> getChildren() {
        return Set.copyOf(children);
    }

    public Set<Workplace> getWorkplaces() {
        return Set.copyOf(workplaces);
    }

    // ============================================================
    // == Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Location other = (Location) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // == Rules
    // ============================================================

    private static Country validateCountry(Country country) {
        if (country == null) ex(KEY_COUNTRY_IS_NULL);
        return country;
    }

    private static LocationType validateType(LocationType type) {
        if (type == null) ex(KEY_TYPE_IS_NULL);
        return type;
    }

    private static Name mapOfName(String name) {
        return Name.of(name, NAME_SPEC, NAME_KEYS, Location::ex);
    }

    private static void ex(String key, Object... args) {
        throw new LocationValidationException(key, args);
    }
}
