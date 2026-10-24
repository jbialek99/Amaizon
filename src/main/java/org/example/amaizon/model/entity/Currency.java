package org.example.amaizon.model.entity;

import jakarta.persistence.*;
import org.example.amaizon.exceptions.validation.CurrencyValidationException;
import org.example.amaizon.model.embeddable.all.Code;
import org.example.amaizon.model.embeddable.all.Name;
import org.example.amaizon.model.embeddable.all.Symbol;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.example.amaizon.model.validators.validation.CurrencyValidation.*;

@Entity
@Table(
        name = "currencies",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_currencies_stable_id", columnNames = "stable_id"),
                @UniqueConstraint(name = "uk_currencies_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_currencies_code", columnNames = "code")
        }
)
public class Currency extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "stable_id", columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID stableId = UUID.randomUUID();

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false))
    private Name name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "code", nullable = false))
    private Code code;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "symbol", nullable = false))
    private Symbol symbol;

    @OneToMany(mappedBy = "currency", fetch = FetchType.LAZY)
    private final Set<Country> countries = new HashSet<>();

    protected Currency() {}

    private Currency(String name, String code, String symbol) {
        this.name = mapOfName(name);
        this.code = mapOfCode(code);
        this.symbol = mapOfSymbol(symbol);
    }

    @PrePersist
    void prePersist() {
        if (stableId == null) stableId = UUID.randomUUID();
    }

    public static Currency create(String name, String code, String symbol) {
        return new Currency(name, code, symbol);
    }

    public void rename(String newName) { this.name = mapOfName(newName); }
    public void changeCode(String newCode) { this.code = mapOfCode(newCode); }
    public void changeSymbol(String newSymbol) { this.symbol = mapOfSymbol(newSymbol); }

    void _addCountry(Country country) { if (country != null) this.countries.add(country); }
    void _removeCountry(Country country) { if (country != null) this.countries.remove(country); }

    public long getId() { return id; }
    public UUID getStableId() { return stableId; }
    public Name getName() { return name; }
    public Code getCode() { return code; }
    public Symbol getSymbol() { return symbol; }
    public Set<Country> getCountries() { return Set.copyOf(countries); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Currency other = (Currency) o;
        return Objects.equals(stableId, other.stableId);
    }

    @Override public int hashCode() { return Objects.hash(stableId); }

    private static Name mapOfName(String raw) {
        return Name.of(raw, NAME_SPEC, NAME_KEYS, Currency::ex);
    }

    private static Symbol mapOfSymbol(String raw) {
        return Symbol.of(raw, SYMBOL_SPEC, SYMBOL_KEYS, Currency::ex);
    }

    private static Code mapOfCode(String raw) {
        return Code.of(raw, CODE_SPEC, CODE_KEYS, Currency::ex);
    }

    private static void ex(String key, Object... args) {
        throw new CurrencyValidationException(key, args);
    }
}
