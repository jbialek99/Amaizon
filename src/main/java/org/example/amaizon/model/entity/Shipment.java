
package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.ShipmentValidationException;
import org.example.amaizon.model.embeddable.all.Code;
import org.example.amaizon.model.enums.Carrier;
import org.example.amaizon.model.enums.ShipmentStatus;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.ShipmentValidation.*;

@Entity
@Table(
        name = "shipments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_shipments_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_shipments_order", columnNames = "order_id")
        },
        indexes = {
                @Index(name = "idx_shipments_order", columnList = "order_id"),
                @Index(name = "idx_shipments_status", columnList = "status")
        }
)
public class Shipment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "stable_id", nullable = false, updatable = false)
    private UUID stableId;


    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier", nullable = false)
    private Carrier carrier;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "tracking_number", nullable = false, length = 120)
    )
    private Code trackingNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    protected Shipment() {
    }

    private Shipment(Order order, Carrier carrier, String trackingNumber) {
        this.stableId = UUID.randomUUID();
        this.status = ShipmentStatus.CREATED;
        this.carrier = validateCarrier(carrier);
        this.trackingNumber = mapOfTrackingNumber(trackingNumber);

        _attachToOrder(validateOrder(order));
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
        if (status == null) status = ShipmentStatus.CREATED;
        if (createdAt == null) createdAt = Instant.now();

        if (order == null) ex(KEY_ORDER_IS_NULL);
        if (carrier == null) ex(KEY_CARRIER_NULL);
        if (trackingNumber == null) ex(KEY_TRACKING_NUMBER_NULL);
    }

    @PreUpdate
    void preUpdate() {
        if (carrier == null) ex(KEY_CARRIER_NULL);
        if (trackingNumber == null) ex(KEY_TRACKING_NUMBER_NULL);
        if (order == null) ex(KEY_ORDER_IS_NULL);
    }

    static Shipment _createForOrder(Order order, Carrier carrier, String trackingNumber) {

        return new Shipment(order, carrier, trackingNumber);
    }

    // ============================================================
    // Relationship Management: Order
    // ============================================================

    void _attachToOrder(Order newOrder) {
        if (newOrder == null) ex(KEY_ORDER_IS_NULL);
        if (Objects.equals(this.order, newOrder)) return;

        Order old = this.order;
        this.order = newOrder;

        if (old != null) old._unlinkShipment();
        newOrder._linkShipment(this);
    }

    void _detach() {
        Order old = this.order;
        if (old == null) return;

        this.order = null;
        old._unlinkShipment();
    }

    // ============================================================
    // Domain methods
    // ============================================================

    public void setTracking(Carrier carrier, String trackingNumber) {
        ensureNotFinal();
        this.carrier = validateCarrier(carrier);
        this.trackingNumber = mapOfTrackingNumber(trackingNumber);
    }

    public void markShipped() {
        ensureNotFinal();

        if (this.status == ShipmentStatus.SHIPPED || this.status == ShipmentStatus.DELIVERED) {
            return;
        }

        this.status = ShipmentStatus.SHIPPED;
        this.shippedAt = Instant.now();

        if (order != null) {
            order._markShipped();
        }
    }

    public void markDelivered() {
        if (status != ShipmentStatus.SHIPPED) ex(KEY_DELIVER_ONLY_AFTER_SHIPPED, status);

        this.status = ShipmentStatus.DELIVERED;
        this.deliveredAt = Instant.now();

        if (order != null) {
            order._markDelivered();
        }
    }

    public void cancel() {
        ensureNotFinal();
        this.status = ShipmentStatus.CANCELLED;
    }

    private void ensureNotFinal() {
        if (status == ShipmentStatus.DELIVERED || status == ShipmentStatus.CANCELLED) {
            ex(KEY_ALREADY_FINAL, status);
        }
    }

    // ============================================================
    // Getters
    // ============================================================

    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public Order getOrder() { return order; }
    public ShipmentStatus getStatus() { return status; }
    public Carrier getCarrier() { return carrier; }
    public Code getTrackingNumber() { return trackingNumber; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getShippedAt() { return shippedAt; }
    public Instant getDeliveredAt() { return deliveredAt; }

    // ============================================================
    // Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Shipment other = (Shipment) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // Mapping + validation helpers
    // ============================================================

    private static Code mapOfTrackingNumber(String trackingNumber) {
        return Code.of(
                trackingNumber,
                TRACKING_NUMBER_SPEC,
                TRACKING_NUMBER_KEYS,
                Shipment::ex
        );
    }

    private static Order validateOrder(Order order) {
        if (order == null) ex(KEY_ORDER_IS_NULL);
        return order;
    }

    private static Carrier validateCarrier(Carrier carrier) {
        if (carrier == null) ex(KEY_CARRIER_NULL);
        return carrier;
    }

    private static void ex(String key, Object... args) {
        throw new ShipmentValidationException(key, args);
    }
}
