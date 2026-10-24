package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.RoleValidationException;
import org.example.amaizon.model.embeddable.all.Description;
import org.example.amaizon.model.embeddable.all.Name;
import org.example.amaizon.model.enums.RoleKey;
import org.example.amaizon.model.enums.RoleScope;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;

import static org.example.amaizon.model.validators.validation.RoleValidation.*;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_roles_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_roles_scope_country_key", columnNames = {"scope", "country_id", "role_key"})
        },
        indexes = {
                @Index(name = "idx_roles_country", columnList = "country_id"),
                @Index(name = "idx_roles_scope_key", columnList = "scope, role_key")
        }
)
public class Role extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", name = "stable_id", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private RoleScope scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_key", nullable = false)
    private RoleKey key;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "name", nullable = false)
    )
    private Name name;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    )
    private Description description;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private final Set<UserRole> userRoles = new HashSet<>();

    protected Role() {
    }

    private Role(RoleScope scope, Country country, RoleKey key, String name, String description) {
        this.scope = validateScope(scope);
        this.key = validateKey(key);
        this.name = mapOfName(name);
        this.description = mapOfDescription(description);
        applyCountryByScope(this.scope, country);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static Role create(RoleScope scope, Country country, RoleKey key, String name, String description) {
        return new Role(scope, country, key, name, description);
    }

    // ============================================================
    // == Domain methods
    // ============================================================

    public void updateAll(RoleScope scope, Country country, RoleKey key, String name, String description) {
        updateScope(scope, country);
        updateKey(key);
        updateName(name);
        updateDescription(description);
    }

    public void updateScope(RoleScope newScope, Country country) {
        RoleScope validated = validateScope(newScope);
        if (this.scope == validated) {
            applyCountryByScope(validated, country != null ? country : this.country);
            return;
        }

        this.scope = validated;
        applyCountryByScope(validated, country);
    }

    public void updateKey(RoleKey newKey) {
        this.key = validateKey(newKey);
    }

    public void updateName(String name) {
        this.name = mapOfName(name);
    }

    public void updateDescription(String description) {
        this.description = mapOfDescription(description);
    }

    public boolean isGlobal() {
        return scope == RoleScope.GLOBAL;
    }

    // ============================================================
    // == Relationship Management: Country
    // ============================================================

    private void applyCountryByScope(RoleScope scope, Country country) {
        if (scope == RoleScope.GLOBAL) {
            if (country != null) ex(KEY_GLOBAL_COUNTRY_MUST_BE_NULL);
            _detachFromCountry();
            return;
        }
        _attachToCountry(validateCountry(country));
    }

    void _attachToCountry(Country newCountry) {
        if (newCountry == null) ex(KEY_COUNTRY_NULL);
        if (Objects.equals(this.country, newCountry)) return;

        Country old = this.country;
        this.country = newCountry;

        if (old != null) old._removeRoleDirect(this);
        newCountry._addRoleDirect(this);
    }

    void _detachFromCountry() {
        Country old = this.country;
        if (old == null) return;

        this.country = null;
        old._removeRoleDirect(this);
    }

    // ============================================================
    // == Relationship Management: UserRoles (inverse)
    // ============================================================

    void _addUserRoleDirect(UserRole ur) {
        if (ur == null) return;
        this.userRoles.add(ur);
    }

    void _removeUserRoleDirect(UserRole ur) {
        if (ur == null) return;
        this.userRoles.remove(ur);
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

    public RoleScope getScope() {
        return scope;
    }

    public RoleKey getKey() {
        return key;
    }

    public Name getName() {
        return name;
    }

    public Description getDescription() {
        return description;
    }

    public Set<UserRole> getUserRoles() {
        return Collections.unmodifiableSet(userRoles);
    }

    // ============================================================
    // == Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Role other = (Role) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // Validation / mapping
    // ============================================================

    private static RoleScope validateScope(RoleScope scope) {
        if (scope == null) ex(KEY_SCOPE_NULL);
        return scope;
    }

    private static RoleKey validateKey(RoleKey key) {
        if (key == null) ex(KEY_KEY_NULL);
        return key;
    }

    private static Country validateCountry(Country country) {
        if (country == null) ex(KEY_COUNTRY_NULL);
        return country;
    }

    private static Name mapOfName(String name) {
        return Name.of(name, NAME_SPEC, NAME_KEYS, Role::ex);
    }

    private static Description mapOfDescription(String description) {
        return Description.of(description, DESC_SPEC, DESC_KEYS, Role::ex);
    }

    private static void ex(String key, Object... args) {
        throw new RoleValidationException(key, args);
    }
}
