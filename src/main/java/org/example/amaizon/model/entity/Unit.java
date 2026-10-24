package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.UnitValidationException;
import org.example.amaizon.model.embeddable.all.Code;
import org.example.amaizon.model.embeddable.all.Symbol;
import org.example.amaizon.model.enums.UnitDimension;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.UnitValidation.*;

@Entity
@Table(
        name = "units",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_units_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_units_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_units_dimension", columnList = "dimension"),
                @Index(name = "idx_units_active", columnList = "is_active")
        }
)
public class Unit extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", name = "stable_id", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "code", nullable = false)
    )
    private Code code;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "symbol", nullable = false)
    )
    private Symbol symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitDimension dimension;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    protected Unit() {}

    private Unit(String code, String symbol, UnitDimension dimension, boolean active) {
        this.code = mapOfCode(code);
        this.symbol = mapOfSymbol(symbol);
        this.dimension = validateDimension(dimension);
        this.isActive = active;
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static Unit create(String code, String symbol, UnitDimension dimension, boolean active) {
        return new Unit(code, symbol, dimension, active);
    }

    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public Code getCode() { return code; }
    public Symbol getSymbol() { return symbol; }
    public UnitDimension getDimension() { return dimension; }
    public boolean isActive() { return isActive; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Unit other = (Unit) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId);
    }

    private static UnitDimension validateDimension(UnitDimension dimension) {
        if (dimension == null) ex(KEY_DIMENSION_IS_NULL);
        return dimension;
    }

    private static Code mapOfCode(String code) {
        return Code.of(code, CODE_SPEC, CODE_KEYS, Unit::ex);
    }

    private static Symbol mapOfSymbol(String symbol) {
        return Symbol.of(symbol, SYMBOL_SPEC, SYMBOL_KEYS, Unit::ex);
    }

    private static void ex(String key, Object... values) {
        throw new UnitValidationException(key, values);
    }
}
