package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.CartItemValidationException;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.CartItemValidation.*;

@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cart_items_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_cart_items_cart_product_country", columnNames = {"cart_id", "product_country_id"})
        },
        indexes = {
                @Index(name = "idx_cart_items_cart", columnList = "cart_id"),
                @Index(name = "idx_cart_items_product_country", columnList = "product_country_id")
        }
)
public class CartItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "stable_id", nullable = false, updatable = false)
    private final UUID stableId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_country_id", nullable = false)
    private ProductCountry productCountry;

    @Column(nullable = false)
    private int quantity;

    protected CartItem() {}

    private CartItem(Cart cart, ProductCountry productCountry, int quantity) {
        this.quantity = validateQuantity(quantity);
        _attachToCart(cart);
        _attachToProductCountry(productCountry);
    }


    public static CartItem create(Cart cart, ProductCountry productCountry, int quantity) {
        return new CartItem(cart, productCountry, quantity);
    }

    // ============================================================
    // Relationship management
    // ============================================================

    void _attachToCart(Cart newCart) {
        if (newCart == null) ex(CART_ITEM_CART_IS_NULL);
        if (Objects.equals(this.cart, newCart)) return;

        Cart old = this.cart;
        this.cart = newCart;

        if (old != null) old._removeItemDirect(this);
        newCart._addItemDirect(this);
    }

    void _attachToProductCountry(ProductCountry pc) {
        if (pc == null) ex(CART_ITEM_PRODUCT_COUNTRY_IS_NULL);
        this.productCountry = pc;
    }

    void _detach() { _detachFromCart(); }

    void _detachFromCart() {
        Cart old = this.cart;
        if (old == null) return;
        this.cart = null;
        old._removeItemDirect(this);
    }

    // ============================================================
    // Domain
    // ============================================================

    public void changeQuantity(int quantity) { this.quantity = validateQuantity(quantity); }

    public void increase(int by) {
        int inc = validateQuantity(by);
        this.quantity += inc;
    }

    // ============================================================
    // Getters
    // ============================================================

    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public Cart getCart() { return cart; }
    public ProductCountry getProductCountry() { return productCountry; }
    public int getQuantity() { return quantity; }

    // ============================================================
    // Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CartItem other = (CartItem) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override public int hashCode() { return Objects.hash(stableId); }

    private static void ex(String key, Object... args) {
        throw new CartItemValidationException(key, args);
    }

    private static int validateQuantity(int qty) {
        if (qty <= 0) ex(CART_ITEM_QTY_INVALID);
        return qty;
    }
}
