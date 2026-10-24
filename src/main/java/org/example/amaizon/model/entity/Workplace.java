package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.WorkplaceValidationException;
import org.example.amaizon.model.embeddable.all.Code;
import org.example.amaizon.model.embeddable.all.DisplayName;

import org.example.amaizon.model.enums.WorkplaceType;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.WorkplaceValidation.*;

@Entity
@Table(
        name = "workplaces",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_workplaces_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_workplaces_country_code", columnNames = {"country_id", "code"})
        },
        indexes = {
                @Index(name = "idx_workplaces_country", columnList = "country_id"),
                @Index(name = "idx_workplaces_location", columnList = "location_id")
        }
)
public class Workplace extends AuditableEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", name = "stable_id", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkplaceType type;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "display_name", nullable = false)
    )
    private DisplayName displayName;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "code", nullable = false)
    )
    private Code code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @OneToMany(mappedBy = "workplace", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<UserWorkplace> userWorkplaces = new HashSet<>();

    protected Workplace() {
    }

    private Workplace(Country country, WorkplaceType type, String displayName, String code, Location location) {
        _attachToCountry(validateCountry(country));
        this.type = validateType(type);
        this.displayName = mapOfDisplayName(displayName);
        this.code = mapOfCode(code);
        changeLocation(location);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static Workplace create(Country country, WorkplaceType type, String displayName, String code, Location location) {
        return new Workplace(country, type, displayName, code, location);
    }

    // ============================================================
    // == Relationship Management: Country
    // ============================================================

    void _attachToCountry(Country newCountry) {
        if (newCountry == null) ex(KEY_COUNTRY_IS_NULL);
        if (Objects.equals(this.country, newCountry)) return;

        Country old = this.country;
        this.country = newCountry;

        if (old != null) old._removeWorkplaceDirect(this);
        newCountry._addWorkplaceDirect(this);
    }



    // ============================================================
    // == Relationship Management: UserWorkplaces (inverse)
    // ============================================================

    void _addUserWorkplaceDirect(UserWorkplace uw) {
        if (uw == null) return;
        this.userWorkplaces.add(uw);
    }

    void _removeUserWorkplaceDirect(UserWorkplace uw) {
        if (uw == null) return;
        this.userWorkplaces.remove(uw);
    }

    // ============================================================
    // == Domain methods
    // ============================================================

    public void updateAll(WorkplaceType type, String displayName, String code, Location location) {
        changeType(type);
        changeDisplayName(displayName);
        changeCode(code);
        changeLocation(location);
    }

    public void changeDisplayName(String displayName) {
        this.displayName = mapOfDisplayName(displayName);
    }

    public void changeType(WorkplaceType type) {
        this.type = validateType(type);
    }

    public void changeCode(String code) {
        this.code = mapOfCode(code);
    }

    public void changeLocation(Location location) {
        _attachToLocation(location);
    }

    void _attachToLocation(Location location) {
        if (location == null) ex(KEY_LOCATION_IS_NULL);
        if (Objects.equals(this.location, location)) return;

        if (this.country != null && location.getCountry() != null
                && !Objects.equals(location.getCountry(), this.country)) {
            ex(KEY_LOCATION_COUNTRY_MISMATCH);
        }

        Location old = this.location;
        this.location = location;

        if (old != null) old._removeWorkplaceDirect(this);
        location._addWorkplaceDirect(this);
    }

    void _detachFromLocation() {
        Location old = this.location;
        if (old == null) return;

        this.location = null;
        old._removeWorkplaceDirect(this);
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

    public WorkplaceType getType() {
        return type;
    }

    public DisplayName getDisplayName() {
        return displayName;
    }

    public Code getCode() {
        return code;
    }

    public Location getLocation() {
        return location;
    }

    public Set<UserWorkplace> getUserWorkplaces() {
        return Set.copyOf(userWorkplaces);
    }

    // ============================================================
    // == Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Workplace other = (Workplace) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // == Mapping / Rules
    // ============================================================

    private DisplayName mapOfDisplayName(String displayName) {
        return DisplayName.of(displayName, DISPLAY_NAME_SPEC, DISPLAY_NAME_KEYS, Workplace::ex);
    }

    private Code mapOfCode(String code) {
        return Code.of(code, CODE_SPEC, CODE_KEYS, Workplace::ex);
    }

    private static Country validateCountry(Country country) {
        if (country == null) ex(KEY_COUNTRY_IS_NULL);
        return country;
    }

    private static WorkplaceType validateType(WorkplaceType type) {
        if (type == null) ex(KEY_TYPE_IS_NULL);
        return type;
    }


    private static void ex(String key, Object... args) {
        throw new WorkplaceValidationException(key, args);
    }
}
