package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.OrderValidationException;
import org.example.amaizon.model.embeddable.all.AddressSnapshot;
import org.example.amaizon.model.embeddable.all.Price;
import org.example.amaizon.model.enums.Carrier;
import org.example.amaizon.model.enums.CustomerType;
import org.example.amaizon.model.enums.OrderStatus;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.example.amaizon.model.validators.validation.OrderValidation.*;

@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_orders_stable_id", columnNames = "stable_id")
        },
        indexes = {
                @Index(name = "idx_orders_user", columnList = "user_id"),
                @Index(name = "idx_orders_country", columnList = "country_id"),
                @Index(name = "idx_orders_status", columnList = "status")
        }
)
public class Order extends AuditableEntity {

    private static final int EMAIL_MIN = 6;
    private static final int EMAIL_MAX = 100;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?!\\.)[A-Za-z0-9._%+-]+(?<!\\.)@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "stable_id", nullable = false, updatable = false)
    private UUID stableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "contact_email", nullable = false, length = EMAIL_MAX)
    private String contactEmail;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "recipientName", column = @Column(name = "shipping_recipient_name", nullable = false, length = 120)),
            @AttributeOverride(name = "phone", column = @Column(name = "shipping_phone", length = 30)),
            @AttributeOverride(name = "countryCode", column = @Column(name = "shipping_country_code", nullable = false, length = 10)),
            @AttributeOverride(name = "countryName", column = @Column(name = "shipping_country_name", nullable = false, length = 120)),
            @AttributeOverride(name = "state", column = @Column(name = "shipping_state")),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city", nullable = false, length = 120)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code", nullable = false, length = 30)),
            @AttributeOverride(name = "streetLine1", column = @Column(name = "shipping_street_line1", nullable = false, length = 200)),
            @AttributeOverride(name = "streetLine2", column = @Column(name = "shipping_street_line2", length = 200))
    })
    private AddressSnapshot shippingAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "recipientName", column = @Column(name = "billing_recipient_name", nullable = false, length = 120)),
            @AttributeOverride(name = "phone", column = @Column(name = "billing_phone", length = 30)),
            @AttributeOverride(name = "countryCode", column = @Column(name = "billing_country_code", nullable = false, length = 10)),
            @AttributeOverride(name = "countryName", column = @Column(name = "billing_country_name", nullable = false, length = 120)),
            @AttributeOverride(name = "state", column = @Column(name = "billing_state")),
            @AttributeOverride(name = "city", column = @Column(name = "billing_city", nullable = false, length = 120)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "billing_postal_code", nullable = false, length = 30)),
            @AttributeOverride(name = "streetLine1", column = @Column(name = "billing_street_line1", nullable = false, length = 200)),
            @AttributeOverride(name = "streetLine2", column = @Column(name = "billing_street_line2", length = 200))
    })
    private AddressSnapshot billingAddress;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "items_subtotal", nullable = false, precision = 19, scale = 2)
    )
    private Price itemsSubtotal;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "shipping_cost", nullable = false, precision = 19, scale = 2)
    )
    private Price shippingCost;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    )
    private Price totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_customer_type", nullable = false, length = 30)
    private CustomerType billingCustomerType;

    @Column(name = "billing_company_name", length = 200)
    private String billingCompanyName;

    @Column(name = "billing_tax_id", length = 40)
    private String billingTaxId;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<OrderItem> items = new HashSet<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Shipment shipment;

    protected Order() {}

    private Order(
            User user,
            Country country,
            Currency currency,
            String contactEmail,
            AddressSnapshot shipping,
            AddressSnapshot billing,
            CustomerType billingCustomerType,
            String billingCompanyName,
            String billingTaxId
    ) {
        this.stableId = UUID.randomUUID();
        this.user = user;
        this.country = validateCountry(country);
        this.currency = validateCurrency(currency);

        this.contactEmail = validateContactEmail(contactEmail);

        this.shippingAddress = validateAddress(shipping);
        this.billingAddress = validateAddress(billing);

        this.billingCustomerType = (billingCustomerType == null)
                ? CustomerType.INDIVIDUAL
                : billingCustomerType;

        this.billingCompanyName = billingCompanyName;
        this.billingTaxId = billingTaxId;

        this.status = OrderStatus.NEW;

        this.itemsSubtotal = mapOfItemSubtotal(BigDecimal.ZERO);
        this.shippingCost = mapOfShippingCost(BigDecimal.ZERO);
        this.totalAmount = mapOfTotalAmount(BigDecimal.ZERO);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = OrderStatus.NEW;

        if (country == null) ex(KEY_COUNTRY_IS_NULL);
        if (currency == null) ex(KEY_CURRENCY_IS_NULL);
        if (shippingAddress == null) ex(KEY_ADDRESS_IS_NULL);
        if (billingAddress == null) ex(KEY_ADDRESS_IS_NULL);

        this.contactEmail = validateContactEmail(this.contactEmail);

        if (billingCustomerType == null) billingCustomerType = CustomerType.INDIVIDUAL;
        if (itemsSubtotal == null) itemsSubtotal = mapOfItemSubtotal(BigDecimal.ZERO);
        if (shippingCost == null) shippingCost = mapOfShippingCost(BigDecimal.ZERO);
        if (totalAmount == null) totalAmount = mapOfTotalAmount(BigDecimal.ZERO);
    }

    public static Order create(
            User user,
            Country country,
            Currency currency,
            String contactEmail,
            AddressSnapshot shipping,
            AddressSnapshot billing,
            CustomerType billingCustomerType,
            String billingCompanyName,
            String billingTaxId
    ) {
        return new Order(user, country, currency, contactEmail, shipping, billing, billingCustomerType, billingCompanyName, billingTaxId);
    }

    // ============================================================
    // Domain methods
    // ============================================================

    public OrderItem addItem(Product product, String productNameSnapshot, String skuSnapshot, BigDecimal unitPrice, int quantity) {
        ensureMutable();
        OrderItem item = OrderItem.create(this, product, productNameSnapshot, skuSnapshot, unitPrice, quantity);
        recalcTotals();
        return item;
    }

    public OrderItem removeItem(OrderItem item) {
        ensureMutable();
        if (item == null) ex(KEY_ITEM_IS_NULL);
        if (!this.items.contains(item)) ex(KEY_ITEM_NOT_FOUND);

        item._detach();
        recalcTotals();
        return item;
    }

    public void setShippingCost(BigDecimal cost) {
        ensureMutable();
        this.shippingCost = mapOfShippingCost(cost == null ? BigDecimal.ZERO : cost);
        recalcTotals();
    }

    public void confirm() {
        if (status != OrderStatus.NEW) {
            throw new IllegalStateException("Only new orders can be confirmed");
        }
        if (items.isEmpty() || shipment == null) {
            throw new IllegalStateException("Order is incomplete");
        }
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
    }

    public void cancel(String reason) {
        if (status != OrderStatus.NEW && status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order cannot be cancelled in status " + status);
        }
        if (payment != null && payment.getStatus() == org.example.amaizon.model.enums.PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Paid order requires refund handling");
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    public Payment startPayment(BigDecimal amount) {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed orders can start payment");
        }
        if (this.payment != null) ex(KEY_PAYMENT_ALREADY_EXISTS);

        BigDecimal a = (amount == null)
                ? this.totalAmount.getValue()
                : mapOfTotalAmount(amount).getValue();

        if (a.compareTo(this.totalAmount.getValue()) != 0) {
            throw new IllegalArgumentException("Payment amount does not match order total");
        }

        return Payment._createForOrder(this, a, this.currency, null);
    }

    public Shipment createShipment(Carrier carrier, String trackingNumber) {
        ensureNotFinalized();
        if (this.shipment != null) ex(KEY_SHIPMENT_ALREADY_EXISTS);

        return Shipment._createForOrder(this, carrier, trackingNumber);
    }

    // ============================================================
    // Relationship management (inverse)
    // ============================================================

    void _addItemDirect(OrderItem item) {
        if (item == null) return;
        this.items.add(item);
    }

    void _removeItemDirect(OrderItem item) {
        if (item == null) return;
        this.items.remove(item);
    }

    void _linkPayment(Payment payment) { this.payment = payment; }
    void _unlinkPayment() { this.payment = null; }

    void _linkShipment(Shipment shipment) { this.shipment = shipment; }
    void _unlinkShipment() { this.shipment = null; }

    // ============================================================
    // Shipment / Payment callbacks
    // ============================================================

    void _markPaid(Instant paidAt) {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed orders can become paid");
        }

        this.status = OrderStatus.PAID;
        this.paidAt = (paidAt != null) ? paidAt : Instant.now();
    }

    void _markShipped() {
        if (this.status != OrderStatus.PAID) {
            throw new IllegalStateException("Only paid orders can be shipped");
        }
        if (payment == null || payment.getStatus() != org.example.amaizon.model.enums.PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Successful payment required");
        }
        this.status = OrderStatus.SHIPPED;
    }

    void _markDelivered() {
        if (this.status != OrderStatus.SHIPPED) {
            ex("ORDER_DELIVER_ONLY_AFTER_SHIPPED", this.status);
        }
        this.status = OrderStatus.DELIVERED;
    }

    // ============================================================
    // Totals
    // ============================================================

    private void recalcTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem i : items) {
            subtotal = subtotal.add(i.getLineTotal());
        }

        this.itemsSubtotal = mapOfItemSubtotal(subtotal);

        BigDecimal ship = (shippingCost == null || shippingCost.getValue() == null)
                ? BigDecimal.ZERO
                : shippingCost.getValue();

        BigDecimal total = subtotal.add(ship);
        this.totalAmount = mapOfTotalAmount(total);

        if (this.totalAmount.getValue().signum() < 0) ex(KEY_TOTAL_NEGATIVE);
    }

    // ============================================================
    // Guards
    // ============================================================

    void ensureMutable() {
        if (status == OrderStatus.PAID
                || status == OrderStatus.CANCELLED
                || status == OrderStatus.SHIPPED
                || status == OrderStatus.DELIVERED) {
            ex(KEY_ORDER_LOCKED, status);
        }
    }

    private void ensureNotFinalized() {
        if (status == OrderStatus.CANCELLED || status == OrderStatus.DELIVERED) {
            ex(KEY_ORDER_LOCKED, status);
        }
    }

    // ============================================================
    // Getters
    // ============================================================

    public CustomerType getBillingCustomerType() { return billingCustomerType; }
    public String getBillingCompanyName() { return billingCompanyName; }
    public String getBillingTaxId() { return billingTaxId; }
    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public User getUser() { return user; }
    public Country getCountry() { return country; }
    public Currency getCurrency() { return currency; }
    public OrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getCancelledAt() { return cancelledAt; }

    public AddressSnapshot getShippingAddress() { return shippingAddress; }
    public AddressSnapshot getBillingAddress() { return billingAddress; }

    public Price getItemsSubtotal() { return itemsSubtotal; }
    public Price getShippingCost() { return shippingCost; }
    public Price getTotalAmount() { return totalAmount; }

    public Set<OrderItem> getItems() { return Set.copyOf(items); }

    public Payment getPayment() { return payment; }
    public Shipment getShipment() { return shipment; }

    public String getContactEmail() { return contactEmail; }

    // ============================================================
    // Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Order other = (Order) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // Price mappers
    // ============================================================

    private static Price mapOfTotalAmount(BigDecimal amount) {
        return Price.of(amount, AMOUNT_KEYS, Order::ex);
    }

    private static Price mapOfShippingCost(BigDecimal shippingCost) {
        return Price.of(shippingCost, SHIPPING_KEYS, Order::ex);
    }

    private static Price mapOfItemSubtotal(BigDecimal itemSubtotal) {
        return Price.of(itemSubtotal, ITEM_KEYS, Order::ex);
    }

    // ============================================================
    // Validation helpers
    // ============================================================

    private static Country validateCountry(Country country) {
        if (country == null) ex(KEY_COUNTRY_IS_NULL);
        return country;
    }

    private static Currency validateCurrency(Currency currency) {
        if (currency == null) ex(KEY_CURRENCY_IS_NULL);
        return currency;
    }

    private static AddressSnapshot validateAddress(AddressSnapshot a) {
        if (a == null) ex(KEY_ADDRESS_IS_NULL);
        return a;
    }

    private static String validateContactEmail(String email) {
        if (email == null || email.isBlank()) ex("ORDER_EMAIL_EMPTY");
        String e = email.trim();
        if (e.length() < EMAIL_MIN || e.length() > EMAIL_MAX) ex("ORDER_EMAIL_LENGTH");
        if (!EMAIL_PATTERN.matcher(e).matches()) ex("ORDER_EMAIL_INVALID");
        return e;
    }

    private static void ex(String key, Object... args) {
        throw new OrderValidationException(key, args);
    }
}
