package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.CartValidationException;
import org.example.amaizon.model.enums.CartStatus;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;

import static org.example.amaizon.model.validators.validation.CartValidation.*;
import static org.example.amaizon.model.validators.validation.OrderItemValidation.KEY_QUANTITY_INVALID;

@Entity
@Table(
        name = "carts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_carts_stable_id", columnNames = "stable_id")
        },
        indexes = {
                @Index(name = "idx_carts_user", columnList = "user_id"),
                @Index(name = "idx_carts_guest", columnList = "guest_token"),
                @Index(name = "idx_carts_country", columnList = "country_id"),
                @Index(name = "idx_carts_status", columnList = "status")
        }
)
public class Cart extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "stable_id", nullable = false, updatable = false)
    private UUID stableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "guest_token", columnDefinition = "uuid")
    private UUID guestToken;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CartStatus status;

    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<CartItem> items = new HashSet<>();

    protected Cart() {}

    private Cart(User user, UUID guestToken, Country country, Currency currency) {
        this.stableId = UUID.randomUUID();
        this.user = user;
        this.guestToken = guestToken;
        this.country = validateCountry(country);
        this.currency = validateCurrency(currency);
        this.status = CartStatus.ACTIVE;
        validateOwner();
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
        if (status == null) status = CartStatus.ACTIVE;

        if (country == null) ex(KEY_COUNTRY_IS_NULL);
        if (currency == null) ex(KEY_CURRENCY_IS_NULL);

        validateOwner();
    }

    public static Cart createForGuest(UUID guestToken, Country country, Currency currency) {
        if (guestToken == null) ex(KEY_GUEST_TOKEN_IS_NULL);
        return new Cart(null, guestToken, country, currency);
    }

    public static Cart createForUser(User user, Country country, Currency currency) {
        return new Cart(validateUser(user), null, country, currency);
    }

    // ============================================================
    // Domain
    // ============================================================

    public void assignToUser(User user) {
        if (user == null) ex(KEY_USER_IS_NULL);
        ensureActive();
        this.user = user;
        this.guestToken = null;
        validateOwner();
    }

    public void checkout() {
        if (this.status == CartStatus.CHECKED_OUT) return;
        ensureActive();
        this.status = CartStatus.CHECKED_OUT;
    }

    public boolean isCheckedOut() { return this.status == CartStatus.CHECKED_OUT; }

    public CartItem addProduct(ProductCountry productCountry, int quantity) {
        ensureActive();
        ProductCountry pc = validateProductCountry(productCountry);
        int qty = validateQuantity(quantity);

        CartItem existing = findItemByProductCountry(pc);
        if (existing != null) {
            existing.increase(qty);
            return existing;
        }
        return CartItem.create(this, pc, qty);
    }

    public CartItem removeProduct(ProductCountry productCountry) {
        ensureActive();
        ProductCountry pc = validateProductCountry(productCountry);

        CartItem existing = findItemByProductCountry(pc);
        if (existing == null) ex(CART_ITEM_NOT_FOUND);

        existing._detach();
        return existing;
    }

    public CartItem changeQuantity(ProductCountry productCountry, int newQuantity) {
        ensureActive();
        ProductCountry pc = validateProductCountry(productCountry);

        CartItem existing = findItemByProductCountry(pc);
        if (existing == null) ex(CART_ITEM_NOT_FOUND);

        if (newQuantity <= 0) {
            existing._detach();
            return existing;
        }

        existing.changeQuantity(newQuantity);
        return existing;
    }



    public CartItem findItemByProductCountry(ProductCountry pc) {
        if (pc == null || pc.getStableId() == null) return null;
        UUID target = pc.getStableId();

        for (CartItem i : items) {
            ProductCountry ipc = i.getProductCountry();
            if (ipc != null && target.equals(ipc.getStableId())) return i;
        }
        return null;
    }

    void _addItemDirect(CartItem item) { if (item != null) items.add(item); }
    void _removeItemDirect(CartItem item) { if (item != null) items.remove(item); }

    // ============================================================
    // Guards
    // ============================================================

    private void ensureActive() {
        if (status != CartStatus.ACTIVE) ex(KEY_CART_LOCKED, status);
    }

    private void validateOwner() {
        if (user == null && guestToken == null) ex(KEY_CART_OWNER_MISSING);
        if (user != null && guestToken != null) ex(KEY_CART_OWNER_CONFLICT);
    }

    // ============================================================
    // Getters
    // ============================================================

    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public User getUser() { return user; }
    public UUID getGuestToken() { return guestToken; }
    public Country getCountry() { return country; }
    public Currency getCurrency() { return currency; }
    public CartStatus getStatus() { return status; }
    public Set<CartItem> getItems() { return Set.copyOf(items); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Cart other = (Cart) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() { return Objects.hash(stableId); }

    private static Country validateCountry(Country country) {
        if (country == null) ex(KEY_COUNTRY_IS_NULL);
        return country;
    }

    private static Currency validateCurrency(Currency currency) {
        if (currency == null) ex(KEY_CURRENCY_IS_NULL);
        return currency;
    }

    private static User validateUser(User user) {
        if (user == null) ex(KEY_USER_IS_NULL);
        return user;
    }

    private static ProductCountry validateProductCountry(ProductCountry productCountry) {
        if (productCountry == null) ex(KEY_PRODUCT_COUNTRY_IS_NULL);
        return productCountry;
    }

    private static int validateQuantity(int quantity) {
        if (quantity <= 0) ex(KEY_QUANTITY_INVALID, quantity);
        return quantity;
    }

    private static void ex(String key, Object... args) {
        throw new CartValidationException(key, args);
    }
}
