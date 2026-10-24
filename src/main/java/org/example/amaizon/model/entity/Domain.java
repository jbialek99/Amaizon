package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.DomainValidationException;
import org.example.amaizon.model.embeddable.all.CcTld;
import org.example.amaizon.model.embeddable.all.Name;
import org.example.amaizon.model.enums.CountryStatus;
import org.example.amaizon.model.enums.DomainStatus;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.DomainValidation.*;

@Entity
@Table(
        name = "domains",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_domains_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_domains_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_domains_cctld", columnNames = "cc_tld"),
                @UniqueConstraint(name = "uk_domains_country_id", columnNames = "country_id")
        },
        indexes = {
                @Index(name = "idx_domains_name", columnList = "name"),
                @Index(name = "idx_domains_cc_tld", columnList = "cc_tld")
        }
)
public class Domain extends AuditableEntity {

    private static void ex(String key) { throw new DomainValidationException(key); }
    private static void ex(String key, Object... values) { throw new DomainValidationException(key, values); }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "stable_id", columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false))
    private Name name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "cc_tld", nullable = false))
    private CcTld ccTld;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DomainStatus status;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    protected Domain() {}

    private Domain(String name, String ccTld, Country country) {
        this.name = mapOfName(name);
        this.ccTld = mapOfCcTld(ccTld);
        this.status = DomainStatus.DRAFT;
        _attachToCountry(validateCountry(country));
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
        if (status == null) status = DomainStatus.DRAFT;
    }

    public static Domain create(String name, String ccTld, Country country) {
        return new Domain(name, ccTld, country);
    }


    public void rename(String newName) { this.name = mapOfName(newName); }
    public void changeCcTld(String newTld) { this.ccTld = mapOfCcTld(newTld); }

    public void activate() {
        if (this.status == DomainStatus.ACTIVE) return;

        this.status = DomainStatus.ACTIVE;

        if (this.country != null && this.country.getStatus() != CountryStatus.ACTIVE) {
            this.country.activate();
        }
    }

    public void deactivate() {
        if (this.status == DomainStatus.INACTIVE) return;

        this.status = DomainStatus.INACTIVE;

        if (this.country != null) {
            this.country._deactivateFromDomain();
        }
    }

    public void changeCountry(Country newCountry) {
        Country validated = validateCountry(newCountry);
        if (this.country == validated) return;

        DomainStatus currentStatus = this.status;

        _attachToCountry(validated);

        if (currentStatus == DomainStatus.ACTIVE && this.country.getStatus() != CountryStatus.ACTIVE) {
            this.country.activate();
        }
    }



    private void _attachToCountry(Country newCountry) {
        if (Objects.equals(this.country, newCountry)) return;

        Domain current = newCountry.getDomain();
        if (current != null && current != this) {
            ex(KEY_COUNTRY_ALREADY_HAS_DOMAIN);
        }

        Country old = this.country;
        if (old != null) old._unlinkDomain();

        this.country = newCountry;

        if (newCountry.getDomain() != this) {
            newCountry._linkDomain(this);
        }
    }


    public long getId() { return id; }
    public UUID getStableId() { return stableId; }

    public Name getName() { return name; }
    public CcTld getCcTld() { return ccTld; }
    public DomainStatus getStatus() { return status; }
    public Country getCountry() { return country; }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Domain other = (Domain) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() { return Objects.hash(stableId); }



    private static Country validateCountry(Country country) {
        if (country == null) ex(KEY_COUNTRY_IS_NULL);
        return country;
    }

    private static CcTld mapOfCcTld(String ccTld) {
        return CcTld.of(ccTld, CCTLD_SPEC, CCTLD_KEYS, Domain::ex);
    }

    private static Name mapOfName(String name) {
        return Name.of(name, NAME_SPEC, NAME_KEYS, Domain::ex);
    }
}
