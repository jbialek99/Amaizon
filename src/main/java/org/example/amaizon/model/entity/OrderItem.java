package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.OrderValidationException;
import org.example.amaizon.model.embeddable.all.DisplayName;
import org.example.amaizon.model.embeddable.all.Price;
import org.example.amaizon.model.embeddable.all.Sku;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.util.Objects;

import static org.example.amaizon.model.validators.validation.OrderItemValidation.*;

@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_order_items_order", columnList = "order_id"),
                @Index(name = "idx_order_items_product", columnList = "product_id")
        }
)
public class OrderItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "product_name", nullable = false)
    )
    private DisplayName productNameSnapshot;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "sku", nullable = false)
    )
    private Sku skuSnapshot;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    )
    private Price unitPrice;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    protected OrderItem() {}

    private OrderItem(
            Order order,
            Product product,
            String productNameSnapshot,
            String skuSnapshot,
            BigDecimal unitPrice,
            int quantity
    ) {
        this.product = validateProduct(product);
        this.productNameSnapshot = mapOfProductName(productNameSnapshot);
        this.skuSnapshot = mapOfSku(skuSnapshot);
        this.unitPrice = mapOfUnitPrice(unitPrice);
        this.quantity = validateQuantity(quantity);

        _attachToOrder(validateOrder(order));
    }

    @PrePersist
    void prePersist() {
        if (productNameSnapshot == null) ex(KEY_PRODUCT_NAME_INVALID);
        if (skuSnapshot == null) ex(KEY_SKU_INVALID);
        if (unitPrice == null) ex(KEY_UNIT_PRICE_INVALID);
        if (quantity <= 0) ex(KEY_QUANTITY_INVALID, quantity);
    }

    public static OrderItem create(
            Order order,
            Product product,
            String productNameSnapshot,
            String skuSnapshot,
            BigDecimal unitPrice,
            int quantity
    ) {
        return new OrderItem(order, product, productNameSnapshot, skuSnapshot, unitPrice, quantity);
    }

    void _attachToOrder(Order newOrder) {
        if (newOrder == null) ex(KEY_ORDER_IS_NULL);
        if (Objects.equals(this.order, newOrder)) return;

        Order old = this.order;
        this.order = newOrder;

        if (old != null) old._removeItemDirect(this);
        newOrder._addItemDirect(this);
    }

    void _detach() {
        Order old = this.order;
        if (old == null) return;

        this.order = null;
        old._removeItemDirect(this);
    }

    public BigDecimal getLineTotal() {
        BigDecimal price = (unitPrice == null || unitPrice.getValue() == null) ? BigDecimal.ZERO : unitPrice.getValue();
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public long getId() { return id; }
    public Order getOrder() { return order; }
    public Product getProduct() { return product; }
    public String getProductNameSnapshot() { return productNameSnapshot.getValue(); }
    public String getSkuSnapshot() { return skuSnapshot.getValue(); }
    public Price getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        OrderItem other = (OrderItem) o;
        return id != 0 && id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    private static DisplayName mapOfProductName(String raw) {
        return DisplayName.of(raw, PRODUCT_NAME_SPEC, PRODUCT_NAME_KEYS, OrderItem::ex);
    }

    private static Sku mapOfSku(String raw) {
        return Sku.of(raw, SKU_SPEC, SKU_KEYS, OrderItem::ex);
    }

    private static Price mapOfUnitPrice(BigDecimal raw) {
        return Price.of(raw, UNIT_PRICE_KEYS, OrderItem::ex);
    }

    private static Order validateOrder(Order order) {
        if (order == null) ex(KEY_ORDER_IS_NULL);
        return order;
    }

    private static Product validateProduct(Product product) {
        if (product == null) ex(KEY_PRODUCT_IS_NULL);
        return product;
    }

    private static int validateQuantity(int quantity) {
        if (quantity <= 0) ex(KEY_QUANTITY_INVALID, quantity);
        return quantity;
    }

    private static void ex(String key, Object... args) {
        throw new OrderValidationException(key, args);
    }
}
