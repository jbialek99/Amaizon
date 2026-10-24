package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.UserRoleValidationException;
import org.example.amaizon.model.embeddable.all.Description;
import org.example.amaizon.model.enums.UserRoleStatus;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import static org.example.amaizon.model.validators.validation.UserRoleValidation.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "user_roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_roles_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_user_roles_user_role", columnNames = {"user_id", "role_id"})
        },
        indexes = {
                @Index(name = "idx_user_roles_user", columnList = "user_id"),
                @Index(name = "idx_user_roles_role", columnList = "role_id")
        }
)
public class UserRole extends AuditableEntity {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", name = "stable_id", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRoleStatus status;

    @Column(nullable = false)
    private Instant validFrom;

    private Instant validTo;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    )
    private Description description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    protected UserRole() {
    }

    private UserRole(User user, Role role, Instant validFrom, Instant validTo, String description) {
        Instant vf = validateValidFrom(validFrom);
        Instant vt = validateValidTo(validTo);
        validateValidRange(vf, vt);

        this.validFrom = vf;
        this.validTo = vt;
        this.description = mapOfDescription(description);
        this.status = UserRoleStatus.ACTIVE;

        _attachToUser(validateUser(user));
        _attachToRole(validateRole(role));
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
        if (status == null) status = UserRoleStatus.ACTIVE;
    }

    public static UserRole create(User user, Role role, Instant validFrom, Instant validTo, String description) {
        return new UserRole(user, role, validFrom, validTo, description);
    }

    // ============================================================
    // == Relationship Management
    // ============================================================

    void _attachToUser(User newUser) {
        if (newUser == null) ex(KEY_USER_IS_NULL);
        if (Objects.equals(this.user, newUser)) return;

        User old = this.user;
        this.user = newUser;

        if (old != null) old._removeUserRoleDirect(this);
        newUser._addUserRoleDirect(this);
    }


    void _attachToRole(Role newRole) {
        if (newRole == null) ex(KEY_ROLE_IS_NULL);
        if (Objects.equals(this.role, newRole)) return;

        Role old = this.role;
        this.role = newRole;

        if (old != null) old._removeUserRoleDirect(this);
        newRole._addUserRoleDirect(this);
    }
    void _detach() {
        _detachFromUser();
        _detachFromRole();
    }

    void _detachFromUser() {
        User old = this.user;
        if (old == null) return;

        this.user = null;
        old._removeUserRoleDirect(this);
    }

    void _detachFromRole() {
        Role old = this.role;
        if (old == null) return;

        this.role = null;
        old._removeUserRoleDirect(this);
    }

    // ============================================================
    // == Domain methods
    // ============================================================

    public void changeValidity(Instant newValidFrom, Instant newValidTo, String description) {
        Instant vf = validateValidFrom(newValidFrom);
        Instant vt = validateValidTo(newValidTo);
        validateValidRange(vf, vt);

        this.validFrom = vf;
        this.validTo = vt;
        this.description = mapOfDescription(description);
        this.status = UserRoleStatus.ACTIVE;
    }

    public void changeStatus(UserRoleStatus newStatus) {
        this.status = validateStatus(newStatus);
    }

    public void changeValidFrom(Instant newValidFrom) {
        Instant vf = validateValidFrom(newValidFrom);
        validateValidRange(vf, this.validTo);
        this.validFrom = vf;
    }

    public void changeValidTo(Instant newValidTo) {
        Instant vt = validateValidTo(newValidTo);
        validateValidRange(this.validFrom, vt);
        this.validTo = vt;
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

    public UserRoleStatus getStatus() {
        return status;
    }

    public User getUser() {
        return user;
    }

    public Role getRole() {
        return role;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidTo() {
        return validTo;
    }

    public String getDescription() {
        return description.getValue();
    }

    // ============================================================
    // == Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserRole other = (UserRole) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // == Validation
    // ============================================================

    private static User validateUser(User user) {
        if (user == null) ex(KEY_USER_IS_NULL);
        return user;
    }

    private static Role validateRole(Role role) {
        if (role == null) ex(KEY_ROLE_IS_NULL);
        return role;
    }

    private static UserRoleStatus validateStatus(UserRoleStatus status) {
        if (status == null) ex(KEY_STATUS_IS_NULL);
        return status;
    }

    private static Instant validateValidFrom(Instant validFrom) {
        if (validFrom == null) ex(KEY_VALID_FROM_IS_NULL);
        return validFrom;
    }

    private static Instant validateValidTo(Instant validTo) {
        return validTo;
    }

    private static void validateValidRange(Instant validFrom, Instant validTo) {
        if (validTo == null) return;
        if (validTo.isBefore(validFrom)) ex(KEY_VALID_TO_BEFORE_VALID_FROM);
    }

    private static Description mapOfDescription(String description) {
        return Description.of(description, DESCRIPTION_SPEC, DESCRIPTION_KEYS, UserRole::ex);
    }

    private static void ex(String key, Object... args) {
        throw new UserRoleValidationException(key, args);
    }
}
