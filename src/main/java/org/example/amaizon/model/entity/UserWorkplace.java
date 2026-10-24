package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.UserWorkplaceValidationException;
import org.example.amaizon.model.embeddable.all.DisplayName;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.UserWorkplaceValidation.*;

@Entity
@Table(
        name = "user_workplaces",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_workplaces_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_user_workplaces_user_workplace", columnNames = {"user_id", "workplace_id"})
        },
        indexes = {
                @Index(name = "idx_user_workplaces_user", columnList = "user_id"),
                @Index(name = "idx_user_workplaces_workplace", columnList = "workplace_id")
        }
)
public class UserWorkplace extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", name = "stable_id", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workplace_id", nullable = false)
    private Workplace workplace;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "position", nullable = false)
    )
    private DisplayName position;

    @Column(name = "primary_workplace", nullable = false)
    private boolean primaryWorkplace = false;

    protected UserWorkplace() {
    }

    private UserWorkplace(User user, Workplace workplace, String position, boolean primaryWorkplace) {
        this.position = mapOfPosition(position);
        this.primaryWorkplace = primaryWorkplace;
        _attachToUser(validateUser(user));
        _attachToWorkplace(validateWorkplace(workplace));
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static UserWorkplace create(User user, Workplace workplace, String position, boolean primaryWorkplace) {
        return new UserWorkplace(user, workplace, position, primaryWorkplace);
    }

    // ============================================================
    // == Relationship Management
    // ============================================================

    void _attachToUser(User newUser) {
        if (newUser == null) ex(KEY_USER_IS_NULL);
        if (Objects.equals(this.user, newUser)) return;

        User old = this.user;
        this.user = newUser;

        if (old != null) old._removeUserWorkplaceDirect(this);
        newUser._addUserWorkplaceDirect(this);
    }

    void _attachToWorkplace(Workplace newWorkplace) {
        if (newWorkplace == null) ex(KEY_WORKPLACE_IS_NULL);
        if (Objects.equals(this.workplace, newWorkplace)) return;

        Workplace old = this.workplace;
        this.workplace = newWorkplace;

        if (old != null) old._removeUserWorkplaceDirect(this);
        newWorkplace._addUserWorkplaceDirect(this);
    }

    void _detach() {
        _detachFromUser();
        _detachFromWorkplace();
    }

    void _detachFromUser() {
        User old = this.user;
        if (old == null) return;

        this.user = null;
        old._removeUserWorkplaceDirect(this);
    }

    void _detachFromWorkplace() {
        Workplace old = this.workplace;
        if (old == null) return;

        this.workplace = null;
        old._removeUserWorkplaceDirect(this);
    }

    // ============================================================
    // == Domain methods
    // ============================================================

    public void setPosition(String position) {
        this.position = mapOfPosition(position);
    }

    public void setPrimaryWorkplace(boolean primaryWorkplace) {
        this.primaryWorkplace = primaryWorkplace;
    }

    public void changeAll(String position, boolean primaryWorkplace) {
        setPosition(position);
        setPrimaryWorkplace(primaryWorkplace);
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

    public User getUser() {
        return user;
    }

    public Workplace getWorkplace() {
        return workplace;
    }

    public DisplayName getPosition() {
        return position;
    }

    public boolean isPrimaryWorkplace() {
        return primaryWorkplace;
    }

    // ============================================================
    // == Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserWorkplace other = (UserWorkplace) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    private DisplayName mapOfPosition(String position) {
        return DisplayName.of(
                position,
                POSITION_SPEC,
                POSITION_KEYS,
                UserWorkplace::ex
        );
    }

    // ============================================================
    // == Rules
    // ============================================================

    private static User validateUser(User user) {
        if (user == null) ex(KEY_USER_IS_NULL);
        return user;
    }

    private static Workplace validateWorkplace(Workplace workplace) {
        if (workplace == null) ex(KEY_WORKPLACE_IS_NULL);
        return workplace;
    }

    private static void ex(String key, Object... args) {
        throw new UserWorkplaceValidationException(key, args);
    }
}
