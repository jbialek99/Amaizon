package org.example.amaizon.model.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.amaizon.model.embeddable.all.AddressSnapshot;
import org.example.amaizon.model.enums.Carrier;
import org.example.amaizon.model.enums.CustomerType;
import org.example.amaizon.model.enums.UsState;

public class CheckoutRequest {


    @NotBlank(message = "{checkout.email.empty}")
    @Size(min = 6, max = 100, message = "{checkout.email.length}")
    @Pattern(
            regexp = "^(?!\\.)[A-Za-z0-9._%+-]+(?<!\\.)@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$",
            message = "{checkout.email.invalid}"
    )
    private String contactEmail;


    @NotBlank private String shippingCountryCode;
    @NotBlank private String shippingCountryName;
    @NotBlank private String billingCountryCode;
    @NotBlank private String billingCountryName;


    @NotBlank(message = "{checkout.recipientName.required}")
    @Size(min = 2, max = 120, message = "{checkout.recipientName.length}")
    private String shippingRecipientName;

    @NotBlank(message = "{checkout.phone.required}")
    @Size(min = 6, max = 30, message = "{checkout.phone.length}")
    private String shippingPhone;


    private String shippingState;

    @NotBlank(message = "{checkout.city.required}")
    @Size(min = 2, max = 120, message = "{checkout.city.length}")
    private String shippingCity;

    @NotBlank(message = "{checkout.postal.required}")
    @Size(min = 3, max = 30, message = "{checkout.postal.length}")
    private String shippingPostalCode;

    @NotBlank(message = "{checkout.street1.required}")
    @Size(min = 3, max = 200, message = "{checkout.street1.length}")
    private String shippingStreetLine1;

    @Size(max = 200, message = "{checkout.street2.length}")
    private String shippingStreetLine2;


    @NotNull(message = "{checkout.customerType.required}")
    private CustomerType billingCustomerType = CustomerType.INDIVIDUAL;

    @Size(min = 2, max = 200, message = "{checkout.companyName.length}")
    private String billingCompanyName;

    @Size(min = 5, max = 40, message = "{checkout.taxId.length}")
    private String billingTaxId;

    @NotBlank(message = "{checkout.recipientName.required}")
    @Size(min = 2, max = 120, message = "{checkout.recipientName.length}")
    private String billingRecipientName;

    @NotBlank(message = "{checkout.phone.required}")
    @Size(min = 6, max = 30, message = "{checkout.phone.length}")
    private String billingPhone;


    private String billingState;

    @NotBlank(message = "{checkout.city.required}")
    @Size(min = 2, max = 120, message = "{checkout.city.length}")
    private String billingCity;

    @NotBlank(message = "{checkout.postal.required}")
    @Size(min = 3, max = 30, message = "{checkout.postal.length}")
    private String billingPostalCode;

    @NotBlank(message = "{checkout.street1.required}")
    @Size(min = 3, max = 200, message = "{checkout.street1.length}")
    private String billingStreetLine1;

    @Size(max = 200, message = "{checkout.street2.length}")
    private String billingStreetLine2;

    @NotNull(message = "{checkout.carrier.required}")
    private Carrier shippingCarrier;


    public AddressSnapshot toShippingSnapshot() {
        return new AddressSnapshot(
                shippingRecipientName,
                shippingPhone,
                shippingCountryCode,
                shippingCountryName,
                parseUsStateOrNull(shippingState),
                shippingCity,
                shippingPostalCode,
                shippingStreetLine1,
                shippingStreetLine2
        );
    }

    public AddressSnapshot toBillingSnapshot() {
        return new AddressSnapshot(
                billingRecipientName,
                billingPhone,
                billingCountryCode,
                billingCountryName,
                parseUsStateOrNull(billingState),
                billingCity,
                billingPostalCode,
                billingStreetLine1,
                billingStreetLine2
        );
    }

    private static UsState parseUsStateOrNull(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;

        try {
            return UsState.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }


    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getShippingCountryCode() { return shippingCountryCode; }
    public void setShippingCountryCode(String shippingCountryCode) { this.shippingCountryCode = shippingCountryCode; }

    public String getShippingCountryName() { return shippingCountryName; }
    public void setShippingCountryName(String shippingCountryName) { this.shippingCountryName = shippingCountryName; }

    public String getBillingCountryCode() { return billingCountryCode; }
    public void setBillingCountryCode(String billingCountryCode) { this.billingCountryCode = billingCountryCode; }

    public String getBillingCountryName() { return billingCountryName; }
    public void setBillingCountryName(String billingCountryName) { this.billingCountryName = billingCountryName; }

    public String getShippingRecipientName() { return shippingRecipientName; }
    public void setShippingRecipientName(String shippingRecipientName) { this.shippingRecipientName = shippingRecipientName; }

    public String getShippingPhone() { return shippingPhone; }
    public void setShippingPhone(String shippingPhone) { this.shippingPhone = shippingPhone; }

    public String getShippingState() { return shippingState; }
    public void setShippingState(String shippingState) { this.shippingState = shippingState; }

    public String getShippingCity() { return shippingCity; }
    public void setShippingCity(String shippingCity) { this.shippingCity = shippingCity; }

    public String getShippingPostalCode() { return shippingPostalCode; }
    public void setShippingPostalCode(String shippingPostalCode) { this.shippingPostalCode = shippingPostalCode; }

    public String getShippingStreetLine1() { return shippingStreetLine1; }
    public void setShippingStreetLine1(String shippingStreetLine1) { this.shippingStreetLine1 = shippingStreetLine1; }

    public String getShippingStreetLine2() { return shippingStreetLine2; }
    public void setShippingStreetLine2(String shippingStreetLine2) { this.shippingStreetLine2 = shippingStreetLine2; }

    public CustomerType getBillingCustomerType() { return billingCustomerType; }
    public void setBillingCustomerType(CustomerType billingCustomerType) { this.billingCustomerType = billingCustomerType; }

    public String getBillingCompanyName() { return billingCompanyName; }
    public void setBillingCompanyName(String billingCompanyName) { this.billingCompanyName = billingCompanyName; }

    public String getBillingTaxId() { return billingTaxId; }
    public void setBillingTaxId(String billingTaxId) { this.billingTaxId = billingTaxId; }

    public String getBillingRecipientName() { return billingRecipientName; }
    public void setBillingRecipientName(String billingRecipientName) { this.billingRecipientName = billingRecipientName; }

    public String getBillingPhone() { return billingPhone; }
    public void setBillingPhone(String billingPhone) { this.billingPhone = billingPhone; }

    public String getBillingState() { return billingState; }
    public void setBillingState(String billingState) { this.billingState = billingState; }

    public String getBillingCity() { return billingCity; }
    public void setBillingCity(String billingCity) { this.billingCity = billingCity; }

    public String getBillingPostalCode() { return billingPostalCode; }
    public void setBillingPostalCode(String billingPostalCode) { this.billingPostalCode = billingPostalCode; }

    public String getBillingStreetLine1() { return billingStreetLine1; }
    public void setBillingStreetLine1(String billingStreetLine1) { this.billingStreetLine1 = billingStreetLine1; }

    public String getBillingStreetLine2() { return billingStreetLine2; }
    public void setBillingStreetLine2(String billingStreetLine2) { this.billingStreetLine2 = billingStreetLine2; }

    public Carrier getShippingCarrier() { return shippingCarrier; }
    public void setShippingCarrier(Carrier shippingCarrier) { this.shippingCarrier = shippingCarrier; }
}
