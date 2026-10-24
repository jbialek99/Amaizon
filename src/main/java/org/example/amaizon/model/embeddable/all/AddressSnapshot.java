package org.example.amaizon.model.embeddable.all;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.example.amaizon.model.enums.UsState;

@Embeddable
public class AddressSnapshot {

    @Column(nullable = false, length = 120)
    private String recipientName;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, length = 10)
    private String countryCode;

    @Column(nullable = false, length = 120)
    private String countryName;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private UsState state;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 30)
    private String postalCode;

    @Column(nullable = false, length = 200)
    private String streetLine1;

    @Column(length = 200)
    private String streetLine2;

    protected AddressSnapshot() {}

    public AddressSnapshot(
            String recipientName,
            String phone,
            String countryCode,
            String countryName,
            UsState state,
            String city,
            String postalCode,
            String streetLine1,
            String streetLine2
    ) {
        this.recipientName = trimToNull(recipientName);
        this.phone = trimToNull(phone);
        this.countryCode = trimToNull(countryCode);
        this.countryName = trimToNull(countryName);
        this.state = state;
        this.city = trimToNull(city);
        this.postalCode = trimToNull(postalCode);
        this.streetLine1 = trimToNull(streetLine1);
        this.streetLine2 = trimToNull(streetLine2);
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public String getRecipientName() { return recipientName; }
    public String getPhone() { return phone; }
    public String getCountryCode() { return countryCode; }
    public String getCountryName() { return countryName; }
    public UsState getState() { return state; }
    public String getCity() { return city; }
    public String getPostalCode() { return postalCode; }
    public String getStreetLine1() { return streetLine1; }
    public String getStreetLine2() { return streetLine2; }
}
