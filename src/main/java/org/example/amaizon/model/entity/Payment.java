package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.PaymentValidationException;
import org.example.amaizon.model.embeddable.all.Code;
import org.example.amaizon.model.embeddable.all.Price;
import org.example.amaizon.model.enums.PaymentProvider;
import org.example.amaizon.model.enums.PaymentStatus;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.PaymentValidation.*;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payments_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_payments_order", columnNames = "order_id")
        },
        indexes = {
                @Index(name = "idx_payments_order", columnList = "order_id"),
                @Index(name = "idx_payments_status", columnList = "status")
        }
)
public class Payment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "stable_id", nullable = false, updatable = false)
    private UUID stableId;


    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 30)
    private PaymentProvider provider;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    )
    private Price amount;


    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "transaction_id", nullable = true, length = 120)
    )
    private Code transactionId;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    protected Payment() {
    }

    private Payment(Order order, BigDecimal amount, Currency currency, PaymentProvider provider) {
        this.stableId = UUID.randomUUID();
        this.status = PaymentStatus.PENDING;
        this.amount = mapOfAmount(amount);
        this.currency = validateCurrency(currency);
        this.provider = provider;

        _attachToOrder(validateOrder(order));
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
        if (status == null) status = PaymentStatus.PENDING;
        if (startedAt == null) startedAt = Instant.now();

        if (order == null) ex(KEY_ORDER_IS_NULL);
        if (currency == null) ex(KEY_CURRENCY_IS_NULL);
        if (amount == null) ex(KEY_AMOUNT_INVALID);
    }

    @PreUpdate
    void preUpdate() {
        if (currency == null) ex(KEY_CURRENCY_IS_NULL);
        if (amount == null) ex(KEY_AMOUNT_INVALID);
    }

    static Payment _createForOrder(Order order, BigDecimal amount, Currency currency, PaymentProvider provider) {
        return new Payment(order, amount, currency, provider);
    }

    // ============================================================
    // Relationship Management: Order
    // ============================================================

    void _attachToOrder(Order newOrder) {
        if (newOrder == null) ex(KEY_ORDER_IS_NULL);
        if (Objects.equals(this.order, newOrder)) return;

        Order old = this.order;
        this.order = newOrder;

        if (old != null) old._unlinkPayment();
        newOrder._linkPayment(this);
    }

    void _detach() {
        Order old = this.order;
        if (old == null) return;

        this.order = null;
        old._unlinkPayment();
    }

    // ============================================================
    // Domain methods
    // ============================================================

    public void setProvider(PaymentProvider provider) {
        ensureNotFinal();
        this.provider = provider;
    }

    public void setTransactionId(String txId) {
        ensureNotFinal();
        this.transactionId = mapOfTransactionId(txId);
    }

    public void markSuccess(String transactionId) {
        if (status == PaymentStatus.SUCCESS) return;


        if (status == PaymentStatus.REFUNDED) ex(KEY_ALREADY_FINAL, status);


        if (status == PaymentStatus.FAILED) ex(PAYMENT_CANNOT_MARK_SUCCESS_AFTER_FAILED, status);

        ensureNotFinal();

        Instant successTime = Instant.now();

        if (this.order == null) {
            throw new IllegalStateException("Payment order missing");
        }
        if (this.order.getStatus() != org.example.amaizon.model.enums.OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order is not ready for payment");
        }

        this.transactionId = mapOfTransactionId(transactionId);
        this.order._markPaid(successTime);
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = successTime;
    }

    public void markFailed(String reason) {
        if (status == PaymentStatus.FAILED) return;
        ensureNotFinal();
        this.status = PaymentStatus.FAILED;
    }

    public void retry() {
        if (status != PaymentStatus.FAILED) {
            throw new IllegalStateException("Only failed payments can be retried");
        }

        if (order == null || order.getStatus() != org.example.amaizon.model.enums.OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order cannot retry payment");
        }

        this.status = PaymentStatus.PENDING;
        this.transactionId = null;
        this.paidAt = null;
    }
    public void markRefunded() {
        if (status != PaymentStatus.SUCCESS) ex(PAYMENT_REFUND_ONLY_AFTER_SUCCESS);
        this.status = PaymentStatus.REFUNDED;
    }

    private void ensureNotFinal() {
        if (status == PaymentStatus.SUCCESS || status == PaymentStatus.REFUNDED) {
            ex(KEY_ALREADY_FINAL, status);
        }
    }

    // ============================================================
    // Getters
    // ============================================================

    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public Order getOrder() { return order; }
    public Currency getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public PaymentProvider getProvider() { return provider; }
    public Price getAmount() { return amount; }
    public Code getTransactionId() { return transactionId; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getPaidAt() { return paidAt; }

    // ============================================================
    // Equality
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Payment other = (Payment) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    // ============================================================
    // Mapping helpers
    // ============================================================

    private static Price mapOfAmount(BigDecimal amount) {
        return Price.of(amount, AMOUNT_KEYS, Payment::ex);
    }

    private static Code mapOfTransactionId(String transactionId) {
        return Code.of(
                transactionId,
                TRANSACTION_ID_SPEC,
                TRANSACTION_ID_KEYS,
                Payment::ex
        );
    }

    // ============================================================
    // Validation helpers
    // ============================================================

    private static Order validateOrder(Order order) {
        if (order == null) ex(KEY_ORDER_IS_NULL);
        return order;
    }

    private static Currency validateCurrency(Currency currency) {
        if (currency == null) ex(KEY_CURRENCY_IS_NULL);
        return currency;
    }

    private static void ex(String key, Object... args) {
        throw new PaymentValidationException(key, args);
    }
}
