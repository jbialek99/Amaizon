package org.example.amaizon.model.embeddable.all;


import jakarta.persistence.Embeddable;

import org.example.amaizon.exceptions.text.TextValueObjectValidationException;
import org.example.amaizon.model.validators.ValidationKeys;
import org.example.amaizon.model.validators.text.TextFactory;
import org.example.amaizon.model.validators.text.TextSpec;

import java.util.Objects;
import java.util.function.BiConsumer;

@Embeddable
public class Symbol {

  private String value;

  private Symbol(String symbol) {
      this.value = symbol;
  }

  protected Symbol() {}

    public static Symbol of(String raw,
                            TextSpec spec,
                            ValidationKeys<TextValueObjectValidationException.Reason> keys,
                            BiConsumer<String, Object[]> throwWithKey){
      return TextFactory.createOrThrow(
              raw,
              spec,
              keys,
              throwWithKey,
              null,
              Symbol::new
      );

    }
    public String getValue() {
        return value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Symbol other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
