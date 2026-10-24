package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.UserValidationException;
import org.example.amaizon.model.embeddable.all.Email;
import org.example.amaizon.model.enums.UserStatus;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

import static org.example.amaizon.model.validators.validation.UserValidation.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_stable_id", columnNames = "stable_id" ),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email" )
        }
)
public class User extends AuditableEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "stable_id", nullable = false, updatable = false, unique = true)
    private UUID stableId = UUID.randomUUID();

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "email", nullable = false, updatable = false)
    )
    private Email email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;


    @Column(name = "registration_date", nullable = false, updatable = false)
    private Instant registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "email_verified_at" )
    private Instant emailVerifiedAt;

    // ============================================================
    // == Relationships
    // ============================================================

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<UserRole> userRoles = new HashSet<>();


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<UserCountryProfile> countryProfiles = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<UserWorkplace> userWorkplaces = new HashSet<>();

    // ============================================================
    // == Constructors
    // ============================================================

    protected User() {
    }

    private User( String email, String passwordHash) {

        this.email = mapOfEmail(email);
        this.passwordHash = validatePasswordHash(passwordHash);
        this.status = UserStatus.ACTIVE;
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
        if (registrationDate == null) registrationDate = Instant.now();
        if (status == null) status = UserStatus.ACTIVE;
    }

    public static User create( String email, String passwordHash) {
        return new User( email, passwordHash);
    }

    // ============================================================
    // == Domain methods (user fields)
    // ============================================================


    public void changePasswordHash(String newPasswordHash) {
        this.passwordHash = validatePasswordHash(newPasswordHash);
    }

    public void markEmailVerified() {
        if (this.emailVerified) return;
        this.emailVerified = true;
        this.emailVerifiedAt = Instant.now();
    }

    // ============================================================
    // == Relationship Management: Roles
    // ============================================================

    public UserRole assignRole(Role newRole, Instant validFrom, Instant validTo, String description) {
        Role role = validateRole(newRole);

        if (hasRole(role)) {
            ex(KEY_ROLE_ALREADY_ASSIGNED, role.getStableId());
        }

        return UserRole.create(this, role, validFrom, validTo, description);
    }

    public UserRole removeRole(Role roleToRemove) {
        Role role = validateRole(roleToRemove);

        UserRole existing = findUserRoleByRole(role);
        if (existing == null) {
            ex(KEY_ROLE_NOT_ASSIGNED, role.getStableId());
        }

        existing._detach();
        return existing;
    }

    public UserRole changeRoleValidity(Role roleToChange, Instant validFrom, Instant validTo, String description) {
        Role role = validateRole(roleToChange);

        UserRole existing = findUserRoleByRole(role);
        if (existing == null) {
            ex(KEY_ROLE_TO_CHANGE_NOT_FOUND, role.getStableId());
        }

        existing.changeValidity(validFrom, validTo, description);
        return existing;
    }

    public boolean hasRole(Role role) {
        return findUserRoleByRole(role) != null;
    }

    public UserRole findUserRoleByRole(Role role) {
        if (role == null) return null;
        for (UserRole ur : userRoles) {
            if (Objects.equals(ur.getRole(), role)) return ur;
        }
        return null;
    }

    void _addUserRoleDirect(UserRole userRole) {
        if (userRole == null) return;
        this.userRoles.add(userRole);
    }

    void _removeUserRoleDirect(UserRole userRole) {
        if (userRole == null) return;
        this.userRoles.remove(userRole);
    }


    // ============================================================
    // == Relationship Management: Country Profiles
    // ============================================================

    public UserCountryProfile createCountryProfile(
            Country country,
            String firstName,
            String lastName,
            String phone,
            String locale,
            Location homeLocation
    ) {
        Country c = validateCountry(country);

        if (hasCountryProfile(c)) {
            ex(KEY_PROFILE_ALREADY_EXISTS, c.getStableId());
        }

        return UserCountryProfile.create(this, c, firstName, lastName, phone, locale, homeLocation);
    }

    public UserCountryProfile updateCountryProfile(
            Country country,
            String firstName,
            String lastName,
            String phone,
            String locale,
            Location homeLocation
    ) {
        Country c = validateCountry(country);

        UserCountryProfile existing = findCountryProfile(c);
        if (existing == null) {
            ex(KEY_PROFILE_NOT_EXISTS, c.getStableId());
        }

        existing.changeAll(firstName, lastName, phone, locale, homeLocation);
        return existing;
    }

    public UserCountryProfile removeCountryProfile(Country country) {
        Country c = validateCountry(country);

        UserCountryProfile existing = findCountryProfile(c);
        if (existing == null) {
            ex(KEY_PROFILE_NOT_EXISTS, c.getStableId());
        }

        existing._detach();
        return existing;
    }

    public boolean hasCountryProfile(Country country) {
        return findCountryProfile(country) != null;
    }

    public UserCountryProfile findCountryProfile(Country country) {
        if (country == null) return null;
        for (UserCountryProfile p : countryProfiles) {
            if (Objects.equals(p.getCountry(), country)) return p;
        }
        return null;
    }

    void _addCountryProfileDirect(UserCountryProfile profile) {
        if (profile == null) return;
        this.countryProfiles.add(profile);
    }

    void _removeCountryProfileDirect(UserCountryProfile profile) {
        if (profile == null) return;
        this.countryProfiles.remove(profile);
    }

    // ============================================================
    // == Relationship Management: Workplaces
    // ============================================================

    public UserWorkplace addWorkplace(Workplace workplace, String position, boolean primary) {
        Workplace wp = validateWorkplace(workplace);

        if (hasWorkplace(wp)) {
            ex(KEY_USER_WORKPLACE_ALREADY_EXISTS, wp.getStableId());
        }

        UserWorkplace uw = UserWorkplace.create(this, wp, position, primary);

        if (primary) {
            for (UserWorkplace other : userWorkplaces) {
                if (other != uw && other.isPrimaryWorkplace()) {
                    other.setPrimaryWorkplace(false);
                }
            }
        }

        return uw;
    }

    public UserWorkplace removeWorkplace(Workplace workplace) {
        Workplace wp = validateWorkplace(workplace);

        UserWorkplace existing = findUserWorkplace(wp);
        if (existing == null) {
            ex(KEY_USER_WORKPLACE_NOT_EXISTS, wp.getStableId());
        }

        existing._detach();
        return existing;
    }

    public UserWorkplace setPrimaryWorkplace(Workplace workplace) {
        Workplace wp = validateWorkplace(workplace);

        UserWorkplace existing = findUserWorkplace(wp);
        if (existing == null) {
            ex(KEY_USER_WORKPLACE_NOT_EXISTS, wp.getStableId());
        }

        for (UserWorkplace other : userWorkplaces) {
            if (other != existing && other.isPrimaryWorkplace()) {
                other.setPrimaryWorkplace(false);
            }
        }

        existing.setPrimaryWorkplace(true);
        return existing;
    }

    public boolean hasWorkplace(Workplace workplace) {
        return findUserWorkplace(workplace) != null;
    }

    public UserWorkplace findUserWorkplace(Workplace workplace) {
        if (workplace == null) return null;
        for (UserWorkplace uw : userWorkplaces) {
            if (Objects.equals(uw.getWorkplace(), workplace)) return uw;
        }
        return null;
    }

    void _addUserWorkplaceDirect(UserWorkplace uw) {
        if (uw == null) return;
        this.userWorkplaces.add(uw);
    }

    void _removeUserWorkplaceDirect(UserWorkplace uw) {
        if (uw == null) return;
        this.userWorkplaces.remove(uw);
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

    public String getEmail() {
        return email.getValue();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getRegistrationDate() {
        return registrationDate;
    }


    public UserStatus getStatus() {
        return status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public Set<UserRole> getUserRoles() {
        return Collections.unmodifiableSet(userRoles);
    }

    public Set<UserCountryProfile> getCountryProfiles() {
        return Set.copyOf(countryProfiles);
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
        User other = (User) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }


    private static Email mapOfEmail(String email){
        return Email.of(
                email,
                EMAIL_SPEC,
                EMAIL_KEYS,
                User::ex
        );
    }

    // ============================================================
    // == Rules
    // ============================================================

    private static String validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            ex(KEY_PASSWORD_IS_EMPTY);
        }
        return passwordHash;
    }

    private static Role validateRole(Role role) {
        if (role == null) ex(KEY_ROLE_IS_NULL);
        return role;
    }

    private static Country validateCountry(Country country) {
        if (country == null) ex(KEY_COUNTRY_IS_NULL);
        return country;
    }

    private static Workplace validateWorkplace(Workplace workplace) {
        if (workplace == null) ex(KEY_WORKPLACE_IS_NULL);
        return workplace;
    }

    private static void ex(String key, Object... args) {
        throw new UserValidationException(key, args);
    }
}
