package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.hibernate.Hibernate;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_evt_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_evt_token", columnNames = "token")
        },
        indexes = {
                @Index(name = "idx_evt_user", columnList = "user_id"),
                @Index(name = "idx_evt_expires_at", columnList = "expires_at")
        }
)
public class EmailVerificationToken extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "stable_id", nullable = false, updatable = false, unique = true)
    private UUID stableId = UUID.randomUUID();

    @Column(name = "token", nullable = false, updatable = false, length = 64)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    protected EmailVerificationToken() {}

    public EmailVerificationToken(User user, String token, Instant expiresAt) {
        this.user = Objects.requireNonNull(user);
        this.token = Objects.requireNonNull(token);
        this.expiresAt = Objects.requireNonNull(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public void markUsed() {
        if (this.usedAt == null) {
            this.usedAt = Instant.now();
        }
    }

    public String getToken() { return token; }
    public User getUser() { return user; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        EmailVerificationToken other = (EmailVerificationToken) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }
}
