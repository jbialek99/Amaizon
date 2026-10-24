package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.CountryValidationException;
import org.example.amaizon.model.embeddable.all.Code;
import org.example.amaizon.model.embeddable.all.LanguageCode;
import org.example.amaizon.model.embeddable.all.Name;
import org.example.amaizon.model.enums.CountryStatus;
import org.example.amaizon.model.enums.DomainStatus;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import static org.example.amaizon.model.validators.validation.CountryValidation.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "countries",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_countries_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_countries_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_countries_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_countries_name", columnList = "name"),
                @Index(name = "idx_countries_code", columnList = "code")
        }
)
public class Country extends AuditableEntity {

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
    @AttributeOverride(name = "value", column = @Column(name = "code", nullable = false))
    private Code code;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "language_code", nullable = false))
    private LanguageCode languageCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CountryStatus status;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @OneToMany(mappedBy = "country", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final Set<ProductCountry> productCountries = new HashSet<>();

    @OneToOne(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    private Domain domain;

    @OneToMany(mappedBy = "country", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private final Set<CategoryCountry> categoryCountries = new HashSet<>();



    @OneToMany(mappedBy = "country", fetch = FetchType.LAZY)
    private final Set<Role> roles = new HashSet<>();


    @OneToMany(mappedBy = "country", fetch = FetchType.LAZY)
    private final Set<UserCountryProfile> userProfiles = new HashSet<>();

    @OneToMany(mappedBy = "country", fetch = FetchType.LAZY)
    private final Set<Workplace> workplaces = new HashSet<>();

    @OneToMany(mappedBy = "country", fetch = FetchType.LAZY)
    private final Set<Location> locations = new HashSet<>();

    protected Country() {
    }

    private Country(String name, String code, String languageCode, Currency currency) {
        this.name = mapOfName(name);
        this.code = mapOfCode(code);
        this.languageCode = mapOfLanguageCode(languageCode);
        this.status = CountryStatus.DRAFT;
        setInternalCurrency(validateCurrency(currency));
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
        if (status == null) status = CountryStatus.DRAFT;
    }

    public static Country create(String name, String code, String languageCode, Currency currency) {
        return new Country(name, code, languageCode, currency);
    }



    public void rename(String newName) {
        this.name = mapOfName(newName);
    }

    public void changeCode(String newCode) {
        this.code = mapOfCode(newCode);
    }

    public void changeLanguageCode(String newDefaultLanguageCode) {
        this.languageCode = mapOfLanguageCode(newDefaultLanguageCode);
    }

    public void activate() {
        if (this.status == CountryStatus.ACTIVE) return;
        internalActivate();
    }

    public void deactivate() {
        if (this.status == CountryStatus.INACTIVE) return;
        internalDeactivate();
    }

    public void changeCurrency(Currency newCurrency) {
        setInternalCurrency(validateCurrency(newCurrency));
    }



    private void setInternalCurrency(Currency newCurrency) {
        if (Objects.equals(this.currency, newCurrency)) return;

        Currency oldCurrency = this.currency;
        this.currency = newCurrency;

        if (oldCurrency != null) oldCurrency._removeCountry(this);
        if (newCurrency != null) newCurrency._addCountry(this);
    }

    void _linkDomain(Domain domain) {
        this.domain = domain;
    }

    void _unlinkDomain() {
        this.domain = null;
    }

    void _addCategoryCountry(CategoryCountry cc) {
        if (cc != null) this.categoryCountries.add(cc);
    }

    void _removeCategoryCountry(CategoryCountry cc) {
        if (cc != null) this.categoryCountries.remove(cc);
    }

    void _deactivateFromDomain() {
        if (this.status == CountryStatus.INACTIVE) return;

        for (ProductCountry pc : this.productCountries) pc.deactivate();
        this.status = CountryStatus.INACTIVE;
    }

    private void internalActivate() {
        if (this.domain == null) ex(KEY_ACTIVATE_WITHOUT_DOMAIN);
        if (this.domain.getStatus() != DomainStatus.ACTIVE) ex(KEY_ACTIVE_WITH_INACTIVE_DOMAIN);

        this.status = CountryStatus.ACTIVE;
        for (ProductCountry pc : this.productCountries) pc.tryAutoActivate();
    }

    private void internalDeactivate() {
        if (this.domain != null && this.domain.getStatus() == DomainStatus.ACTIVE) {
            ex(KEY_DEACTIVATE_WITH_ACTIVE_DOMAIN);
        }

        this.status = CountryStatus.INACTIVE;
        for (ProductCountry pc : this.productCountries) pc.deactivate();
    }

    void _addProductCountry(ProductCountry pc) {
        if (pc != null) this.productCountries.add(pc);
    }

    void _removeProductCountry(ProductCountry pc) {
        if (pc != null) this.productCountries.remove(pc);
    }



    void _addRoleDirect(Role role) {
        if (role == null) return;
        this.roles.add(role);
    }

    void _removeRoleDirect(Role role) {
        if (role == null) return;
        this.roles.remove(role);
    }


    void _addUserProfileDirect(UserCountryProfile profile) {
        if (profile == null) return;
        this.userProfiles.add(profile);
    }

    void _removeUserProfileDirect(UserCountryProfile profile) {
        if (profile == null) return;
        this.userProfiles.remove(profile);
    }

    void _addWorkplaceDirect(Workplace workplace) {
        if (workplace == null) return;
        this.workplaces.add(workplace);
    }

    void _removeWorkplaceDirect(Workplace workplace) {
        if (workplace == null) return;
        this.workplaces.remove(workplace);
    }

    void _addLocationDirect(Location location) {
        if (location == null) return;
        this.locations.add(location);
    }

    void _removeLocationDirect(Location location) {
        if (location == null) return;
        this.locations.remove(location);
    }



    public long getId() {
        return id;
    }

    public UUID getStableId() {
        return stableId;
    }

    public Name getName() {
        return name;
    }

    public Code getCode() {
        return code;
    }

    public LanguageCode getLanguageCode() {
        return languageCode;
    }

    public CountryStatus getStatus() {
        return status;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Domain getDomain() {
        return domain;
    }

    public Set<CategoryCountry> getCategoryCountries() {
        return Set.copyOf(categoryCountries);
    }

    public Set<ProductCountry> getProductCountries() {
        return Set.copyOf(productCountries);
    }


    public Set<Role> getRoles() {
        return Set.copyOf(roles);
    }


    public Set<UserCountryProfile> getUserProfiles() {
        return Set.copyOf(userProfiles);
    }

    public Set<Workplace> getWorkplaces() {
        return Set.copyOf(workplaces);
    }

    public Set<Location> getLocations() {
        return Set.copyOf(locations);
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Country other = (Country) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }


    private static void ex(String key, Object... values) {
        throw new CountryValidationException(key, values);
    }



    private static Currency validateCurrency(Currency currency) {
        if (currency == null) ex(KEY_CURRENCY_IS_NULL);
        return currency;
    }

    private static Name mapOfName(String name) {
        return Name.of(name, NAME_SPEC, NAME_KEYS, Country::ex);
    }

    private static LanguageCode mapOfLanguageCode(String languageCode) {
        return LanguageCode.of(languageCode, LANG_CODE_SPEC, LANG_CODE_KEYS, Country::ex);
    }

    private static Code mapOfCode(String code) {
        return Code.of(code, CODE_SPEC, CODE_KEYS, Country::ex);
    }
}
