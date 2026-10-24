package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.UserCountryProfileValidationException;
import org.example.amaizon.model.embeddable.all.DisplayName;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import static org.example.amaizon.model.validators.validation.UserCountryProfileValidation.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "user_country_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ucp_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_ucp_user_country", columnNames = {"user_id", "country_id"})
        },
        indexes = {
                @Index(name = "idx_ucp_user", columnList = "user_id"),
                @Index(name = "idx_ucp_country", columnList = "country_id")
        }
)
public class UserCountryProfile extends AuditableEntity {

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
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "first_name", nullable = false)
    )
    private DisplayName firstName;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "last_name", nullable = false)
    )
    private DisplayName lastName;

    @Column(length = 30)
    private String phone;

    @Column(length = 15)
    private String locale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_location_id")
    private Location homeLocation;

    protected UserCountryProfile() {
    }

    private UserCountryProfile(
            User user,
            Country country,
            String firstName,
            String lastName,
            String phone,
            String locale,
            Location homeLocation
    ) {
        _attachToUser(validateUser(user));
        _attachToCountry(validateCountry(country));
        this.firstName = mapOfFirstName(firstName);
        this.lastName = mapOfLastName(lastName);
        this.phone = phone;
        this.locale = locale;
        this.homeLocation = homeLocation;
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static UserCountryProfile create(
            User user,
            Country country,
            String firstName,
            String lastName,
            String phone,
            String locale,
            Location homeLocation
    ) {
        return new UserCountryProfile(user, country, firstName, lastName, phone, locale, homeLocation);
    }

    // ============================================================
    // == Relationship Management
    // ============================================================

    void _attachToUser(User newUser) {
        if (newUser == null) ex(KEY_USER_IS_NULL);
        if (Objects.equals(this.user, newUser)) return;

        User old = this.user;
        this.user = newUser;

        if (old != null) old._removeCountryProfileDirect(this);
        newUser._addCountryProfileDirect(this);
    }

    void _attachToCountry(Country newCountry) {
        if (newCountry == null) ex(KEY_COUNTRY_IS_NULL);
        if (Objects.equals(this.country, newCountry)) return;

        Country old = this.country;
        this.country = newCountry;

        if (old != null) old._removeUserProfileDirect(this);
        newCountry._addUserProfileDirect(this);
    }


    void _detach() {
        _detachFromUser();
        _detachFromCountry();
    }

    void _detachFromUser() {
        User old = this.user;
        if (old == null) return;

        this.user = null;
        old._removeCountryProfileDirect(this);
    }

    void _detachFromCountry() {
        Country old = this.country;
        if (old == null) return;

        this.country = null;
        old._removeUserProfileDirect(this);
    }

    // ============================================================
    // == Domain methods
    // ============================================================

    public void changeFirstName(String newFirstName) {
        this.firstName = mapOfFirstName(newFirstName);
    }

    public void changeLastName(String newLastName) {
        this.lastName = mapOfLastName(newLastName);
    }

    public void changePhone(String phone) {
        this.phone = phone;
    }

    public void changeLocale(String locale) {
        this.locale = locale;
    }

    public void changeHomeLocation(Location homeLocation) {
        this.homeLocation = homeLocation;
    }

    public void changeAll(String firstName, String lastName, String phone, String locale, Location homeLocation) {
        changeFirstName(firstName);
        changeLastName(lastName);
        changePhone(phone);
        changeLocale(locale);
        changeHomeLocation(homeLocation);
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

    public Country getCountry() {
        return country;
    }

    public DisplayName getFirstName() {
        return firstName;
    }

    public DisplayName getLastName() {
        return lastName;
    }


    public String getPhone() {
        return phone;
    }

    public String getLocale() {
        return locale;
    }

    public Location getHomeLocation() {
        return homeLocation;
    }

    // ============================================================
    // == Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserCountryProfile other = (UserCountryProfile) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }


    private DisplayName mapOfFirstName(String firstName) {
        return DisplayName.of(
                firstName,
                NAME_SPEC,
                FIRST_NAME_KEYS,
                UserCountryProfile::ex
        );
    }

    private DisplayName mapOfLastName(String lastName) {
        return DisplayName.of(
                lastName,
                NAME_SPEC,
                LAST_NAME_KEYS,
                UserCountryProfile::ex
        );
    }

    // ============================================================
    // == Rules
    // ============================================================

    private static User validateUser(User user) {
        if (user == null) ex(KEY_USER_IS_NULL);
        return user;
    }

    private static Country validateCountry(Country country) {
        if (country == null) ex(KEY_COUNTRY_IS_NULL);
        return country;
    }

    private static void ex(String key, Object... args) {
        throw new UserCountryProfileValidationException(key, args);
    }
}
